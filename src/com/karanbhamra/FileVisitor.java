//*******************************************************************
//  Karandeep Bhamra
//  October 9, 2018
//  COMP585
//  FileVisitor is responsible for visiting each file and filters the
//  file based on the provided file extensions and adds the found
//  results to a matched ArrayList.
//*******************************************************************
package com.karanbhamra;

import org.apache.log4j.Logger;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileVisitor extends SimpleFileVisitor<Path>
{

    private final Logger logger;
    private ArrayList<String> matchedFiles;
    private ArrayList<String> extensions;

    public FileVisitor(Logger logger, ArrayList<String> extensions)
    {
        this.logger = logger;
        matchedFiles = new ArrayList<String>();
        this.extensions = extensions;

    }

    public ArrayList<String> getMatchedFiles()
    {
        return matchedFiles;
    }

    private static String getFileExtension(File file)
    {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    // visit the given file and match it against the provided extensions and add it correct
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
    {

        if (basicFileAttributes.isRegularFile())
        {

            for (var extension : extensions)
            {
                String currentFileExtension = getFileExtension(path.toFile());
                if (currentFileExtension.equals(extension))
                {
                    matchedFiles.add(path.toString());
                }
            }
        }

        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException ioException)
    {
        return CONTINUE;
    }

    // Log any files that failed to visited
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException ioException)
    {

        logger.error("Error occurred in visiting " + path.toString());
        return CONTINUE;
    }
}