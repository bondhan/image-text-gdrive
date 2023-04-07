package com.bondhan.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import okhttp3.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {
    private static final String APPLICATION_NAME = "usaha123";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String ENGLISH = "eng";
    private static final String CHINESE = "chs";
    private static final String FOLDER_URL = "folderUrl";
    private static final String API_KEY = "apiKey";
    private static final String FONT_FILE_CHINESE = "./src/main/resources/fonts/chinese/arphic/chinese.stsong.ttf";
    private static final String ENGINE = "engine";


    public static void main(String... args) {
        List<String> chinese = new ArrayList<>();
        List<String> english = new ArrayList<>();

        try {
            String apiKey = getFromProperty(API_KEY);

            Drive drive = getDriveSvc(getCredential());
            List<File> images = listImageFiles(drive, getFromProperty(FOLDER_URL));

            if (images != null) {
                for (File image : images) {
                    System.out.printf("Processing file: %s id: %s \n", image.getName(), image.getId());
                    String base64Img = getCodedBase64(drive, image.getId(), getExtensionByStringHandling(image.getName()));

                    english.addAll(populateText(base64Img, ENGLISH, apiKey));
                    chinese.addAll(populateText(base64Img, CHINESE, apiKey));
                }
            }

            createEngPdfFile("./output/" + getFromProperty(ENGINE) + "/" + getTimestamp("yyyyMMddHHmmssSSS") + ENGLISH + ".pdf", english);
            createChsPdfFile("./output/" + getFromProperty(ENGINE) + "/" + getTimestamp("yyyyMMddHHmmssSSS") + CHINESE + ".pdf", chinese);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("english: ");
        printList(english);

        System.out.println("chinese: ");
        printList(chinese);
    }

    /**
     * populateText will collect the words from api call and store them in a list
     * */
    private static List<String> populateText(String base64Img, String lang, String apiKey) throws IOException {
        List<String> words = new ArrayList<>();

        OcrSpace resEng = getOcr(base64Img, lang, false, getFromProperty(ENGINE), apiKey);
        String text = resEng.parsedResults.get(0).parsedText;

        String[] lines = text.split("\r\n");
        for (String line : lines) {
            if (lang.equals(ENGLISH) && isEnglishText(line)) {
                words.add(line);
            } else if (lang.equals(CHINESE) && !isEnglishText(line)) {
                words.add(line);
            }
        }

        return words;
    }

    /**
     * getFromProperty will return the value given the key parameter from
     * config.properties file
     * */
    private static String getFromProperty(String key) {
        Properties properties = new Properties();

        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.getProperty(key);
    }

    /**
     * isEnglishText checks if a string is english text or no
     * */
    public static boolean isEnglishText(String str) {
        String regex = "^[\\x00-\\x7F]*$";
        return str.matches(regex);
    }

    /***
     * getCredential will return the google oauth credential
     */
    private static Credential getCredential() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.
                Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).
                setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).
                setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * parseFolderIdFromUrl will return the id given url folder
     * */
    private static String parseFolderIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * getDriveSvc will return the google drive service given google oauth credential
     * */
    private static Drive getDriveSvc(Credential credential) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * listImageFiles will list all the image types jpeg and png in the google drive folder
     * */
    private static List<File> listImageFiles(Drive drive, String folderUrl) throws IOException {
        String folderId = parseFolderIdFromUrl(folderUrl);

        FileList files = drive.files().list()
                .setQ("mimeType='image/jpeg' or mimeType='image/png'")
                .setQ("'" + folderId + "' in parents")
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> imageFiles = files.getFiles();
        if (imageFiles == null || imageFiles.isEmpty()) {
            System.out.println("No image files found.");
            return null;
        }

        return imageFiles;
    }

    /**
     * getCodedBase64 will return the base64 of an image in google folder given it's id
     * */
    private static String getCodedBase64(Drive drive, String fileId, String extension) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

        byte[] imageBytes = outputStream.toByteArray();

        String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);

        if (extension.toLowerCase().contains("jpg")) {
            return "data:image/jpeg;base64," + base64EncodedImage;
        } else if (extension.toLowerCase().contains("png")) {
            return "data:image/png;base64," + base64EncodedImage;
        }

        throw new IOException("invalid image extension");
    }

    /**
     * getExtensionByStringHandling will return file extension
    * */
    private static String getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
    }

    /**
     * will do an http call to ocr.space and unmarshal the response into an object
    * */
    private static OcrSpace getOcr(String imageBase64, String lang, Boolean searchable, String engine, String apikey) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String search = "false";
        if (searchable) {
            search = "true";
        }

        if (Integer.parseInt(engine) > 5) {
            engine = "1";
        }

        RequestBody requestBody = new FormBody.Builder().add("apikey", apikey)
                .add("language", lang).add("iscreatesearchablepdf", search)
                .add("OCREngine", engine).add("base64Image", imageBase64).build();

        Request request = new Request.Builder().url("https://api.ocr.space/parse/image").post(requestBody).build();

        Response response = client.newCall(request).execute();

        ResponseBody responseBody = response.body();

        ObjectMapper objectMapper = new ObjectMapper();
        OcrSpace ocrS = objectMapper.readValue(responseBody.byteStream(), OcrSpace.class);

        return ocrS;
    }

    /**
     * createFile will create the file and its parent folder
    * */
    private static java.io.File createFile(String filename) {
        java.io.File file = new java.io.File(filename);
        file.getParentFile().mkdirs();

        return file;
    }

    /**
     * createChsPdfFile will create pdf file given list of words
     * */
    private static void createChsPdfFile(String filename, List<String> words) throws IOException {
        java.io.File  file = createFile(filename);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdf, pageSize);

        PdfFont pdfFontChinese = PdfFontFactory.createFont(FONT_FILE_CHINESE, PdfEncodings.IDENTITY_H, true);
        Color black = new DeviceRgb(0, 0, 0); // Black

        Paragraph paragraph = new Paragraph().setFont(pdfFontChinese);
        Text space = new Text(" ").setFontColor(black);

        for (String word : words) {
            Text text = new Text(word).setFontColor(black).setFont(pdfFontChinese);
            paragraph.add(text).add(space);
        }

        document.add(paragraph);
        document.close();
    }

    /**
     * createEngPdfFile will create pdf file given list of words
     * */
    private static void createEngPdfFile(String filename, List<String> words) throws IOException {
        java.io.File  file = createFile(filename);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdf, pageSize);

        PdfFont font = PdfFontFactory.createFont();
        Color black = new DeviceRgb(0, 0, 0);
        Color blue = new DeviceRgb(0, 0, 255);

        Paragraph paragraph = new Paragraph().setFont(font);
        Text space = new Text(" ").setFontColor(black);

        for (String word : words) {
            Text text;
            if (word.contains("o") || word.contains("O")) {
                text = new Text(word).setFontColor(blue);
            } else {
                text = new Text(word).setFontColor(black);
            }
            paragraph.add(text).add(space);
        }

        document.add(paragraph);
        document.close();
    }

    /**
     * getTimestamp will return the timestamp in string given the format
     * */
    private static String getTimestamp(String format) {
        Instant timestamp = Instant.now();

        LocalDateTime date =
                LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(format);

        return date.format(formatter);
    }

    /**
     * printList will print the value of List
     * */
    private static void printList(List<String> data) {
        for (String str : data) {
            System.out.println(str);
        }
    }

}

