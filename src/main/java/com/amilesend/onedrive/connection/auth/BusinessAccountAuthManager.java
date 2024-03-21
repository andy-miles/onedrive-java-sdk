/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2024 Andy Miles (andy.miles@amilesend.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.amilesend.onedrive.connection.auth;

import com.amilesend.onedrive.parse.GsonFactory;
import com.amilesend.onedrive.resource.discovery.DiscoverServiceResponse;
import com.amilesend.onedrive.resource.discovery.Service;
import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.FORM_DATA;

/**
 * Manager that is responsible for obtaining and refreshing tokens to interact with a business
 * OneDrive account for a specific resource. Note: This does not manage the initial stages of the OAUTH request
 * flow and instead relies on a provided auth code or a pre-existing refresh token.
 *
 * <p>Example initializing with an auth code:</p>
 * <pre>
 * BusinessAccountAuthManager authManager = BusinessAccountAuthManager.builderWithAuthCode()
 *         .httpClient(client) // the OKHttpClient instance
 *         .gsonFactory(gsonFactory) // preconfigured Gson instances
 *         .clientId(clientId) // the client ID of your application
 *         .clientSecret(clientSecret) // the client secret of your application
 *         .redirectUrl(redirectUrl) // the redirect URL for OAUTH flow
 *         .resourceId(resourceId) // the specific resource identifier
 *         .authCode(authCode) // The obtained auth code from initial OAUTH handshake
 *         .buildWithAuthCode();
 * </pre>
 *
 * <p>
 * Once the BusinessAccountAuthManager is created, the next step is to discover available services to connect to and
 * authorize access to a chosen service:
 * <pre>
 * List<Service> services = authManager.getServices();
 * authManager.authorizeService(services.get(0));
 * </pre>
 *
 * <p>Example initializing with an AuthInfo (pre-existing refresh token):</p>
 * <pre>
 * BusinessAccountAuthManager authManager = BusinessAccountAuthManager.builderWithAuthInfo()
 *         .httpClient(client)
 *         .gsonFactory(gsonFactory)
 *         .clientId(clientId)
 *         .clientSecret(clientSecret)
 *         .redirectUrl(redirectUrl)
 *         .resourceId(resourceId) // the specific resource identifier
 *         .authInfo(authInfo) // Instead of an auth code, an AuthInfo object is used to obtain the refresh token
 *         .buildWithAuthInfo();
 * </pre>
 * Note: Existing persisted authInfo is only valid for a single resource. If the user is to access a different
 * resource than the one that that is persisted with the auth tokens, then new access tokens are required and
 * should invoke the {@code builderWithAuthCode()} flow.
 *
 * @see AuthInfo
 */
public class BusinessAccountAuthManager implements AuthManager {
    private static final String ENDPOINT_SUFFIX = "/_api/v2.0";
    private static final String RESOURCE_DISCOVERY_URL = "https://api.office.com/discovery/";
    private static final String SERVICE_INFO_URL_SUFFIX = "v2.0/me/services";
    private static final String RESOURCE_BODY_PARAM = "resource";

    /**
     * The client identifier.
     */
    private final String clientId;
    /**
     * The client secret.
     */
    private final String clientSecret;
    /**
     * The redirect URL.
     */
    private final String redirectUrl;
    /**
     * The underlying HTTP client.
     */
    private final OkHttpClient httpClient;
    /**
     * The GSON instance used for JSON serialization.
     */
    private final String authBaseTokenUrl;
    /**
     * The URL to query for a list of authorized services.
     */
    private final String discoveryBaseTokenUrl;
    private final Object lock = new Object();

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private String resourceId;

    /**
     * The current authentication information.
     */
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private volatile AuthInfo authInfo;

    /**
     * Used to initialize and manage authentication for a given auth code.
     */
    @Builder(builderClassName = "BuilderWithAuthCode",
            buildMethodName = "buildWithAuthCode",
            builderMethodName = "builderWithAuthCode")
    private BusinessAccountAuthManager(
            @NonNull final OkHttpClient httpClient,
            final String authBaseTokenUrl,
            final String discoveryBaseTokenUrl,
            final String clientId,
            final String clientSecret,
            final String redirectUrl,
            final String authCode) {
        Validate.notBlank(authCode, "authCode must not be blank");
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");

        this.authBaseTokenUrl = StringUtils.isNotBlank(authBaseTokenUrl) ? authBaseTokenUrl : AUTH_TOKEN_URL;
        this.discoveryBaseTokenUrl = StringUtils.isNotBlank(discoveryBaseTokenUrl)
                ? discoveryBaseTokenUrl
                : RESOURCE_DISCOVERY_URL;
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        resourceId = this.discoveryBaseTokenUrl;
        authInfo = redeemToken(authCode);
    }

    /**
     * Used to manage authentication for an existing AuthInfo that contains a refresh token.
     */
    @Builder(builderClassName = "BuilderWithAuthInfo",
            builderMethodName = "builderWithAuthInfo",
            buildMethodName = "buildWithAuthInfo")
    private BusinessAccountAuthManager(
            @NonNull final OkHttpClient httpClient,
            final String authBaseTokenUrl,
            final String discoveryBaseTokenUrl,
            final String clientId,
            final String clientSecret,
            final String redirectUrl,
            @NonNull final AuthInfo authInfo) {
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");
        Validate.notBlank(authInfo.getResourceId(), "AuthInfo.resourceId must not be blank");

        this.authBaseTokenUrl = StringUtils.isNotBlank(authBaseTokenUrl) ? authBaseTokenUrl : AUTH_TOKEN_URL;
        this.discoveryBaseTokenUrl = StringUtils.isNotBlank(discoveryBaseTokenUrl)
                ? discoveryBaseTokenUrl
                : RESOURCE_DISCOVERY_URL;
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authInfo = authInfo;
        resourceId = authInfo.getResourceId();
        refreshToken();
    }

    @Override
    public boolean isAuthenticated() {
        return authInfo != null;
    }

    @Override
    public boolean isExpired() {
        if (!isAuthenticated()) {
            throw new AuthManagerException("Not authenticated");
        }

        return System.currentTimeMillis() >= authInfo.getExpiresIn();
    }

    @Override
    public AuthInfo getAuthInfo() {
        refreshIfExpired();
        return authInfo;
    }

    @Override
    public String getAuthenticatedEndpoint() {
        if (StringUtils.isBlank(resourceId) || RESOURCE_DISCOVERY_URL.equals(resourceId)) {
            throw new AuthManagerException("No valid service resource ID is defined. " +
                    "You must authenticate with a specific service. Was authenticateService() invoked?");
        }

        return resourceId + ENDPOINT_SUFFIX;
    }

    @Override
    public AuthInfo redeemToken(final String authCode) {
        Validate.notBlank(authCode, "authCode must not be blank");
        synchronized (lock) {
            return authInfo =
                    AuthManager.fetchAuthInfo(httpClient, new Request.Builder()
                                    .url(authBaseTokenUrl)
                                    .header(CONTENT_TYPE, FORM_DATA.toString())
                                    .post(new FormBody.Builder()
                                            .add(CLIENT_ID_BODY_PARAM, clientId)
                                            .add(CLIENT_SECRET_BODY_PARAM, clientSecret)
                                            .add(REDIRECT_URI_BODY_PARAM, redirectUrl)
                                            .add(RESOURCE_BODY_PARAM, resourceId)
                                            .add(AUTH_CODE_BODY_ARAM, authCode)
                                            .add(GRANT_TYPE_BODY_PARAM, AUTH_CODE_GRANT_TYPE_BODY_PARAM_VALUE)
                                            .build())
                                    .build())
                            .copyWithResourceId(resourceId);
        }
    }

    @Override
    public AuthInfo refreshToken() {
        synchronized (lock) {
            return authInfo =
                    AuthManager.fetchAuthInfo(httpClient, new Request.Builder()
                                    .url(authBaseTokenUrl)
                                    .header(CONTENT_TYPE, FORM_DATA_CONTENT_TYPE)
                                    .post(new FormBody.Builder()
                                            .add(CLIENT_ID_BODY_PARAM, clientId)
                                            .add(CLIENT_SECRET_BODY_PARAM, clientSecret)
                                            .add(REDIRECT_URI_BODY_PARAM, redirectUrl)
                                            .add(RESOURCE_BODY_PARAM, resourceId)
                                            .add(REFRESH_TOKEN_BODY_PARAM, authInfo.getRefreshToken())
                                            .add(GRANT_TYPE_BODY_PARAM, REFRESH_TOKEN_GRANT_TYPE_BODY_PARAM_VALUE)
                                            .build())
                                    .build())
                            .copyWithResourceId(resourceId);
        }
    }

    /**
     * Fetches the list of services available to the authenticated business user. Note:
     * Once a service is selected, you must invoke {@link #authenticateService(Service)} prior to
     * instantiating a new connection.
     *
     * @return the list of services
     */
    public List<Service> getServices() {
        final Request request = new Request.Builder()
                .url(discoveryBaseTokenUrl + SERVICE_INFO_URL_SUFFIX)
                .header("Authorization", getAuthInfo().getFullToken())
                .header("Accept", "application/json;odata=verbose ")
                .get()
                .build();

        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AuthManagerException("Unsuccessful service discovery request: " + response);
                }

                final String json = response.body().string();
                return GsonFactory.getInstance()
                        .getInstanceForServiceDiscovery()
                        .fromJson(json, DiscoverServiceResponse.class)
                        .getServices();
            }
        } catch (final AuthManagerException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AuthManagerException("Error fetching service info: " + ex.getMessage(), ex);
        }
    }

    /**
     * Authenticates with the given {@code service} and refreshes the auth tokens so that a new
     * {@code OneDriveConnection} can be used to access the service.
     *
     * @param service the service to authenticate
     */
    public void authenticateService(@NonNull final Service service) {
        Validate.notBlank(service.getServiceResourceId(), "service#getServiceResourceId() must not be blank");
        resourceId = service.getServiceResourceId();
        refreshToken();
    }
}
