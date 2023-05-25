## Image Analyzer GDrive

This is for sawitpro technical test.

How to run:
1. Create Google credentials, download the json file and copy to `src/main/resource/credentials.json` (see below)
2. Register to [ocr.space](https://ocr.space/OCRAPI) to get your `apiKey` and update the `config.properties` file with `apiKey`
3. Upload the images in the folder `images` to google folder and copy the link after you share
4. In the config.properties put the google drive folder to `folderUrl`, therefore your `config.properties` will be as follow:
    ```bash
    apiKey=K123232131232
    engine=3
    folderUrl=https://drive.google.com/drive/folders/1dmRnkHjthDBF94sAS5HGzkuVIwChNhKk
    ```
4. download the library, compile and package the program with maven
    ```bash
    mvn compile && mvn package
    ```
5. Run the jar file
   ```bash
   java -jar target/image_analyzer_gdrive-1.0-SNAPSHOT.jar
   ```
   output:
   ```bash
   > java -jar target/image_analyzer_gdrive-1.0-SNAPSHOT.jar
   Processing file: ImageWithWords4.jpg id: 11mXYRo0A7nyKRLFl_hAgMqG9DUs27bMX
   Processing file: ImageWithWords1.jpg id: 1sN4XhLJuiGx8ZFiI1iHv8oWtXQ91zFgn
   Processing file: ImageWithWords3.jpg id: 1T74EsNcjpl8Y6ShOeRd83pMxecVL6k_A
   Processing file: ImageWithWords2.png id: 1O1ySI26ima6w6jtnlLPpAeaPqhPzY_32
   ```
6. Output PDF files will be in the `output/1` for engine 1 and `output/3` if you are using engine 3 on ocr.space 

### Limitations:
<ul>
<li>max image size is 1MB due to free plan on ocr.space</li>
<li>only png or jpeg has been tested</li>
<li>the extraction quality is depending on the engine you can select</li>
</ul>

### Troubleshoot
- If you cannot access the gdrive folder you need to delete the file `StoredCredential` in folder `tokens` and reauthenticate
- Make sure your internet connection is stable
- use library `itext7-core` version `7.1.5` if not the chinese font will not work

### Creating google credential to access google drive
To get a Google credential file, you'll need to create a new project in the Google Cloud Console and enable the Google Drive API for the project. Here are the steps to create and download the credentials file:
<ol>
<li>Go to the Google Cloud Console.</li>
<li>Create a new project by clicking on the "Select a project" dropdown at the top of the page and selecting "New Project". Give the project a name and click "Create".</li>
<li>Enable the Google Drive API for the project. Go to the project dashboard by clicking on the project name in the top bar, then click "Enable APIs and Services" and search for "Google Drive API". Click on the API in the search results, then click "Enable".</li>
<li>Create a new set of credentials for the project. From the Google Drive API page, click on "Create credentials" and select "Service account key". Fill in the required information and select "JSON" as the key type, then click "Create".</li>
<li>Download the JSON credentials file. The file should download automatically, but if it doesn't, click on the download button next to the name of the credentials file.</li>
<li>Store the credentials file securely in your project directory. Make sure to keep the credentials file secret, as it contains sensitive information that can be used to access your Google Drive account.</li>
</ol>
