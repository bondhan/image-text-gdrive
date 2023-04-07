package com.bondhan.java;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class OcrSpace {
    @JsonProperty("ParsedResults")
    public ArrayList<ParsedResult> parsedResults;
    @JsonProperty("OCRExitCode")
    public int oCRExitCode;
    @JsonProperty("IsErroredOnProcessing")
    public boolean isErroredOnProcessing;
    @JsonProperty("ProcessingTimeInMilliseconds")
    public String processingTimeInMilliseconds;
    @JsonProperty("SearchablePDFURL")
    public String searchablePDFURL;
}

 class Line {
    @JsonProperty("LineText")
    public String lineText;
    @JsonProperty("Words")
    public ArrayList<Word> words;
    @JsonProperty("MaxHeight")
    public double maxHeight;
    @JsonProperty("MinTop")
    public double minTop;
}

class ParsedResult {
    @JsonProperty("TextOverlay")
    public TextOverlay textOverlay;
    @JsonProperty("TextOrientation")
    public String textOrientation;
    @JsonProperty("FileParseExitCode")
    public int fileParseExitCode;
    @JsonProperty("ParsedText")
    public String parsedText;
    @JsonProperty("ErrorMessage")
    public String errorMessage;
    @JsonProperty("ErrorDetails")
    public String errorDetails;
}

 class Word {
    @JsonProperty("WordText")
    public String wordText;
    @JsonProperty("Left")
    public double left;
    @JsonProperty("Top")
    public double top;
    @JsonProperty("Height")
    public double height;
    @JsonProperty("Width")
    public double width;
}

 class TextOverlay {
    @JsonProperty("Lines")
    public ArrayList<Line> lines;
    @JsonProperty("HasOverlay")
    public boolean hasOverlay;
    @JsonProperty("Message")
    public String message;
}

