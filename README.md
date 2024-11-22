<a name="readme-top"></a>
<!-- Template Credit: Othneil Drew (https://github.com/othneildrew),
                      https://github.com/othneildrew/Best-README-Template/tree/master -->
<!-- PROJECT SHIELDS -->
<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

</div>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/andy-miles/onedrive-java-sdk">
    <img src="images/icon.png" alt="Logo" width="128" height="128">
  </a>

  <h3 align="center">ondrive-java-sdk</h3>

  <p align="center">
    A Java SDK to access OneDrive drives and files.
    <br />
    <a href="https://www.amilesend.com/onedrive-java-sdk"><strong>Maven Project Info</strong></a>
    -
    <a href="https://www.amilesend.com/onedrive-java-sdk/apidocs/index.html"><strong>Javadoc</strong></a>
    <br />
    <a href="https://github.com/andy-miles/onedrive-java-sdk/issues">Report Bug</a>
    -
    <a href="https://github.com/andy-miles/onedrive-java-sdk/issues">Request Feature</a>
  </p>
</div>


<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#feature-highlights">Feature Highlights</a></li>
        <li><a href="#unsupported-features">Current Unsupported Features</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#getting-started">Getting Started</a></li>
        <li><a href="#main-objects">Primary Classes</a></li>
        <li><a href="#recipes">Recipes</a></li>
      </ul>
    </li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
# About The Project

A configurable and extensible SDK for Java programmatic access to a user's OneDrive account via the [Microsoft
Graph API](https://learn.microsoft.com/en-us/onedrive/developer). This SDK is still in active development, but most 
documented APIs are implemented.

<a name="feature-highlights"></a>
## Feature Highlights
1. OAuth user authentication out-of-the-box for use by client-side desktop applications
   1. Or you can roll your own OAuth solution to obtain the auth code and persist tokens for server-to-server use-cases
2. Automatic credential token refresh support
3. Synchronous and asynchronous file transfer operations with a customizable transfer progress callback interface for extensibility
4. Business account support to access group resources as well as SharePoint document libraries
   1. Note: This is currently untested with a real business account.  Please file a <a href="https://github.com/andy-miles/onedrive-java-sdk/issues">bug report</a> if any issues are encountered.

<a name="unsupported-features"></a>
## Current Unsupported Features
1. [Upload sessions](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_createuploadsession) for uploading larges files in segments
2. [Remote uploads from URL](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_upload_url) (in preview)
3. [Obtaining content for a Thumbnail](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/thumbnail)

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<a name="usage"></a>
# Usage
<a name="getting-started"></a>
## Getting Started

Per the [App Registration](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/getting-started/app-registration) documentation, your application 
needs to be registered via the [Azure Apps Registration Page](https://aka.ms/AppRegistrations/).

Key configuration  to note:
1. The following <strong>delegated</strong> API permissions are recommended: <code>Files.ReadWrite.All</code> <code>User.Read</code> <code>offline_access</code>
   1. The following <strong>delegated</strong> API permission are recommended for business accounts in order to access SharePoint sites: <code>Sites.ReadWrite.All</code> <code>Sites.Manage.All</code> <code>Sites.FullControl.All</code>
2. If using the default OAuth receiver to handle the redirect for auth code grants, then set the redirect URL to <code>http://localhost:8890/Callback </code>
3. Generate your own client secret, and record your application's client ID and client secret value.
   1. You can save this as a JAR bundled resource within your project named <code>/ms-onedrive-credentials.json</code> and should be formatted as:

      ```json
      {
        "clientId" : "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        "clientSecret" : "xxxxxxxxxxxxxxxxxxxxxxxxxxx"
      }
      ```

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<a name="main-objects"></a>
## Primary Classes
The primary classes used to interact with a OneDrive account is modeled as a tree structure and is as follows:
1. [OneDrive](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/OneDrive.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/OneDrive.html)) to obtain user accessible drives
      1. [BusinessOneDrive](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/BusinessOneDrive.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/BusinessOneDrive.html)) to obtain business-related user accessible drives and sites
2. [Drive](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/resource/Drive.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/resource/Drive.html)) to access folders
3. [DriveFolder](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/resource/DriveFolder.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/resource/DriveFolder.html)) to manage folders, access subfolders, and upload new files
4. [DrivePackage](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/resource/DrivePackage.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/resource/DrivePackage.html)) to manage folders, access subfolders, and upload new files for packages (OneNote)
5. [DriveFile](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/resource/DriveFile.java) ([javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/resource/DriveFile.html)) to manage, upload (as a new version), and download a file

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<a name="recipes"></a>
## Recipes
### Obtaining an authenticated <code>OneDrive</code> object
```java
// Used to initiate the OAuth flow, persist refreshed tokens, or use persisted refresh tokens.
OneDriveFactoryStateManager factoryStateManager = OneDriveFactoryStateManager.builder()
        .stateFile(Paths.get("./OneDriveUserState.json")) // Path to save/read user auth tokens
        .build();
try {
    OneDrive oneDrive = factoryStateManager.getInstance();
    // Access drives, folders, and files
} finally {
    // Updates persisted auth tokens if refreshed during usage
    factoryStateManager.saveState();
}
```
For more information, please refer to [javadoc](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/OneDriveFactoryStateManager.html) 
or [source](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/OneDriveFactoryStateManager.java).

### Customizing user auth token state persistence
<details>
<summary>AuthInfoStore example</summary>

The [AuthInfoStore](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/connection/auth/store/AuthInfoStore.html)
interface provides the ability to implement custom logic to manage user auth token state persistence (e.g., database, remote service, etc.).
The default implementation is [SingleUserFileBasedAuthInfoStore](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/connection/auth/store/SingleUserFileBasedAuthInfoStore.java)
that saves the auth state to the file system. It also serves as an example on how to implement your own AuthInfoStore implementation.

```java
public class MyAuthInfoStore implements AuthInfoStore {
    private DependentDestinationStore myStore;

    @Override
    public void store(String id, AuthInfo authInfo) throws IOException {
        myStore.save(id, authInfo);
    }

    @Override
    public AuthInfo retrieve(String id) throws IOException {
        myStore.get(id);
    }
}

OneDriveFactoryStateManager factoryStateManager = OneDriveFactoryStateManager.builder()
           .authInfoStore(new MyAuthInfoStore()))
           .build();
try {
    OneDrive oneDrive = factoryStateManager.getInstance();
    // Access drives, folders, and files
} finally {
    // Updates persisted auth tokens if refreshed during usage to your custom store
    factoryStateManager.saveState();
}

```

</details>

#### Using the provided SingleUserEncryptedFileBasedAuthInfoStore

<details>
<summary>SingleUserEncryptedFileBasedAuthInfoStore example</summary>

This SDK also provides a [SingleUserEncryptedFileBasedAuthInfoStore](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/connection/auth/store/SingleUserEncryptedFileBasedAuthInfoStore.java)
implementation that encrypts and saves the auth state to the file system. A specified keystore file path along with associated passwords must be specified.
If the key store file does not exist, a new one will be created along with a newly generated crypto key that is used to encrypt and decrypt the AuthInfo.

```java
Path myOneDriveUserState = Paths.get("./MyOneDriveUserState.json")
Path myKeyStorePath = Paths.get("./MyKeyStoreFile");
char[] myKeyStorePassword = System.getProperty("MyKeyStorePassword").toCharArray();
char[] myAuthStateCryptoKeyPassword = System.getProperty("MyCryptoKeyPassword").toCharArray();

// Helper to manage storage of the crypto key
KeyStoreHelper keyStoreHelper = new KeyStoreHelper(myKeyStorePath, myKeyStorePassword);
// Helper to encrypt/decrypt AuthInfo contents
CryptoHelper cryptoHelper =
        new CryptoHelperFactory(keyStoreHelper, myAuthStateCryptoKeyPassword).newInstance();
// The AuthInfoStore implementation that encrypts + saves and reads + decrypts
// from the filesystem.
SingleUserEncryptedFileBasedAuthInfoStore authInfoStore =
        new SingleUserEncryptedFileBasedAuthInfoStore(myOneDriveUserState, cryptoHelper);

OneDriveFactoryStateManager factoryStateManager = OneDriveFactoryStateManager.builder()
        .authInfoStore(authInfoStore)
        .build();
```

</details>

### Customizing the HTTP client configuration

<details>
<summary>OkHttpClientBuilder example</summary>

If your use-case requires configuring the underlying <code>OkHttpClient</code> instance (e.g., configuring your own 
SSL cert verification, proxy, and/or connection timeouts), you can configure the client with the provided
[OkHttpClientBuilder](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/connection/http/OkHttpClientBuilder.java),
or alternatively with [OkHttp's builder](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/).

```java
OkHttpClient httpClient = OkHttpClientBuilder.builder()
        // Custom trust manager for self/internally signed SSL/TLS certs
        .trustManager(myX509TrustManager)
        // Custom hostname verification for SSL/TLS endpoints
        .hostnameVerifier(myHostnameVerifier)
        .proxy(myProxy, myProxyUsername, myProxyPassword) // Proxy config
        .connectTimeout(8000L) // connection timeout in milliseconds
        .readTimeout(5000L) // read timeout in milliseconds
        .writeTimeout(5000L) // write timeout in milliseconds
        .build();
OneDriveFactoryStateManager factoryStateManager = OneDriveFactoryStateManager.builder()
        .httpClient(httpClient)
        .stateFile(Paths.get("./OneDriveUserState.json")) // Path to save/read user auth tokens
        .build();

try {
    OneDrive oneDrive = factoryStateManager.getInstance();
    // Access drives, folders, and files
} finally {
    // Updates persisted auth tokens if refreshed during usage
    factoryStateManager.saveState();
}
```

</details>

### Obtaining a <code>OneDrive</code> with a custom OAuth flow

<details>
<summary>Custom OAuth flow</summary>

Once you obtain the <code>authCode</code>, you can initialize a new <code>OneDrive</code> directly via:
```java
OneDrive oneDrive = new OneDrive(OneDriveConnectionBuilder.newInstance()
      .clientId(clientId) // Your application's client identifier
      .clientSecret(clientSecret) // Your application's client secret
      .redirectUrl(redirectUrl) // Your custom redirect URL that was used to obtain the authCode
      .build(authCode));
```

While token refresh is automated during the runtime lifecycle of the <code>OneDrive</code> object, persisting
the user <code>AuthInfo</code> is required for subsequent initialization(s) to prevent the user from granting
authorization each time an instance is created until the user explicitly revokes the grant or when it expires.  Example of subsequent initialization with <code>AuthInfo</code>
```java
AuthInfo authInfo = getAuthInfo(); // Obtain the persisted AuthInfo from your application
OneDrive oneDrive = new OneDrive(OneDriveConnectionBuilder.newInstance()
        .clientId(clientId) // Your application's client identifier
        .clientSecret(clientSecret) // Your application's client secret
        // Your custom redirect URL that was used to obtain the authCode
        .redirectUrl(redirectUrl)
        .build(authInfo));
authInfo = oneDrive.getAuthInfo(); // Gets the updated tokens after refresh
```

</details>

### Obtaining a <code>BusinessOneDrive</code> instance

<details>
<summary>BusinessOneDrive initialization</summary>

```java
OkHttpClient httpClient = new OkHttpClientBuilder().build();

BusinessAccountAuthManager authManager = BusinessAccountAuthManager.builderWithAuthCode()
        .authCode(myAuthCode) // Auth code from your OAuth handshake
        .clientId(myClientId) // Your application's client identifier
        .clientSecret(myClientSecret) // Your application's client secret
        .httpClient(httpClient)
        .redirectUrl(myRedirectUrl) // The redirect URL associated with your OAuth flow
        .buildWithAuthCode();

// Discover and authenticate with a registered service
List<Service> services = authManager.getServices();
authManager.authenticateService(services.get(0));

// Create a new BusinessOneDrive instance
BusinessOneDrive oneDrive = new BusinessOneDrive(
    OneDriveConnectionBuilder.newInstance()
        .httpClient(httpClient)
        .authManager(authManager)
        .build(authManager.getAuthInfo()));

// Access business related resources
Site rootSite = oneDrive.getRootSite();
```


</details>

### Obtaining list of contents of a user's default drive
```java
DriveFolder rootFolder = oneDrive.getUserDrive().getRootFolder();

// All children containing either DriveFile or DriveFolder types
List<? extends DriveItemType> allContents = rootFolder.getChildren();

// All child folders with the root folder as parent
List<DriveFolder> driveFolders = rootFolder.getChildFolders();

// All child files with the root folder as parent
List<DriveFile> driveFiles = rootFolder.getChildFiles()
```

### Upload a new file to the "Documents" folder
```java
DriveFolder documentsFolder = documentsFolder = root.search("Documents").stream()
        .filter(DriveItemType::isFolder)
        .findFirst()
        .map(DriveFolder.class::cast)
        .orElseThrow(() -> new IllegalStateException("Documents not found"));

DriveFile myDrivefile = documentsFolder.upload(new File("./MyFile.zip"));
```

#### Upload a new file asynchronously
```java
DriveFileUploadExecution uploadedExec = documentsFolder.uploadAsync(new File("./MyFile.zip"));

// Block on upload completion
DriveFile myDrivefile = uploadedExec.get();
```

### Download a file to the specified folder
```java
DriveFile myDriveFile = root.search("MyFile.zip").stream()
        .filter(DriveItemType::isFile)
        .findFirst()
        .map(DriveFile.class::cast)
        .orElseThrow(() -> new IllegalStateException("MyFile.zip not found"));

myDriveFile.download(Path.of("./"));
```

#### Download a file asynchronously
```java
DriveFileDownloadExecution downloadExec = myDriveFile.downloadAsync(Paths.get("./"));

// Block on download completion
long downloadedBytes = downloadExec.get();
```

### Monitoring transfer progress
The [TransferProgressCallback](https://www.amilesend.com/onedrive-java-sdk/apidocs/com/amilesend/onedrive/connection/file/TransferProgressCallback.html)
interface provides the ability to implement custom logic on update, completion, or failure scenarios (e.g., GUI updates) during file transfers.
The default implementation is [LogProgressCallback](https://github.com/andy-miles/onedrive-java-sdk/blob/main/src/main/java/com/amilesend/onedrive/connection/file/LogProgressCallback.java)
that logs transfer updates to the configured log via SLF4J. It also serves as an example
on how to implement your own TransferProgressCallback implementation.
```java
public class MyProgressCallback implements TransferProgressCallback {
   @Override
   public void onUpdate(long currentBytes, long totalBytes) { ... }

   @Override
   public void onFailure(Throwable cause) { ... }

   @Override
   public void onComplete(long bytesTransferred) { ... }
}

// Upload
DriveFile myDrivefile = myFolder.upload(new File("./MyFile.zip"), new MyProgressCallback());
DriveFileUploadExecution uploadedExec =
        myFolder.uploadAsync(new File("./MyFile.zip"), new MyProgressCallback());

// Download
myFile.download(Path.of("./"), new MyProgressCallback());
DriveFileDownloadExecution downloadExec =
        myFile.downloadAsync(Paths.get("./"), new MyProgressCallback());
```

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<!-- ROADMAP -->
## Roadmap
- [x] ~~Add functional test coverage (use of a MockWebServer)~~
- [ ] Add integration test coverage
- [x] ~~Group and Site based access for non-personal accounts~~
- [X] ~~Add an interface to access and persist tokens for the OneDriveFactoryStateManager (e.g., tokens stored via a database or service)~~ (v0.1.1)
- [X] ~~Obtaining [embeddable file previews](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_preview)~~ (v0.1.2)
- [ ] [Remote uploads from URL](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_upload_url) (in preview)
- [ ] [Obtaining content for a Thumbnail](https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/thumbnail)
- [ ] Add configuration of a retry policy + strategy to support automatic retries for retriable errors

See the [open issues](https://github.com/andy-miles/onedrive-java-sdk/issues) for a full list of proposed features and known issues.

<div align="right">(<a href="#readme-top">back to top</a>)</div>


<!-- CONTRIBUTING -->
## Contributing

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<!-- LICENSE -->
## License

Distributed under the GPLv3 license. See [LICENSE](https://github.com/andy-miles/onedrive-java-sdk/blob/main/LICENSE) for more information.

<div align="right">(<a href="#readme-top">back to top</a>)</div>


<!-- CONTACT -->
## Contact

Andy Miles - andy.miles (at) amilesend.com

Project Link: [https://github.com/andy-miles/onedrive-java-sdk](https://github.com/andy-miles/onedrive-java-sdk)

<div align="right">(<a href="#readme-top">back to top</a>)</div>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/andy-miles/onedrive-java-sdk.svg?style=for-the-badge
[contributors-url]: https://github.com/andy-miles/onedrive-java-sdk/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/andy-miles/onedrive-java-sdk.svg?style=for-the-badge
[forks-url]: https://github.com/andy-miles/onedrive-java-sdk/network/members
[stars-shield]: https://img.shields.io/github/stars/andy-miles/onedrive-java-sdk.svg?style=for-the-badge
[stars-url]: https://github.com/andy-miles/onedrive-java-sdk/stargazers
[issues-shield]: https://img.shields.io/github/issues/andy-miles/onedrive-java-sdk.svg?style=for-the-badge
[issues-url]: https://github.com/andy-miles/onedrive-java-sdk/issues
[license-shield]: https://img.shields.io/github/license/andy-miles/onedrive-java-sdk.svg?style=for-the-badge
[license-url]: https://github.com/andy-miles/onedrive-java-sdk/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/andy-miles
[product-screenshot]: images/screenshot.png
[Next.js]: https://img.shields.io/badge/next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white
[Next-url]: https://nextjs.org/
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[Vue-url]: https://vuejs.org/
[Angular.io]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[Angular-url]: https://angular.io/
[Svelte.dev]: https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00
[Svelte-url]: https://svelte.dev/
[Laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[Laravel-url]: https://laravel.com
[Bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[Bootstrap-url]: https://getbootstrap.com
[JQuery.com]: https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white
[JQuery-url]: https://jquery.com 
