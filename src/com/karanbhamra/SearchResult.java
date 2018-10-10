package com.karanbhamra;

public class SearchResult
{
    private int lineNumber;
    private String fileName;
    private String line;

    public SearchResult(int lineNum, String file, String line)
    {
        this.lineNumber = lineNum;
        this.fileName = file;
        this.line = line;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getLine()
    {
        return line;
    }

    public Object[] toArray()
    {
        Object[] arr = {lineNumber, fileName, line};

        return arr;
    }
}
