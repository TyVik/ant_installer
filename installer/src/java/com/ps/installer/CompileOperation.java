package com.ps.installer;

import java.io.File;
import java.io.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.util.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

/* Класс реализует таск для ant, задача которого - компиляция операций для HAS */
public class CompileOperation extends Task {
    private File inDir; // каталог с деревом операций
    private File outFile; // файл-результат в виде xml.

    public CompileOperation() {
        super();
    }

    public void setDir(File directory) {
        this.inDir = directory;
    }

    public void setFile(File file) {
        this.outFile = file;
    }

    private void compileOperations(String operationsDir, PrintWriter outXML) throws IOException {
        String operationHeader = "<operation file_full_path=\"%s\" file_name=\"%s\" file_ext=\"%s\" file_created=\"%s\"><![CDATA[";
        File f = new File(operationsDir);
        String[] sDirList = f.list();
        int i;
        for(i = 0; i < sDirList.length; i++) {
            String fullPath = operationsDir + File.separator + sDirList[i];
            File f1 = new File(fullPath);
            if(f1.isFile()) {
                List<String> data = Files.readAllLines(Paths.get(f1.getCanonicalPath()), Charset.forName("windows-1251"));
                outXML.write(String.format(operationHeader, f1.getCanonicalPath(), sDirList[i], sDirList[i].substring(sDirList[i].lastIndexOf(".") + 1), new SimpleDateFormat("dd.M.yyyy HH:mm").format(new Date(f1.lastModified()))) + "\n");
                Iterator<String> itr = data.iterator();
                while (itr.hasNext()) {
                    outXML.write(itr.next() + "\n");
                }
                outXML.write("]]></operation>\n");
                log(fullPath);
            } else {
                compileOperations(fullPath, outXML);
            }
        }
    }

    public void execute() {
        log(String.format("Compiling operations from \"%s\" to %s", this.inDir.getPath(), this.outFile.getName()));
        if (!this.inDir.isDirectory()) {
            throw new BuildException(String.format("\"%s\" must be directory!", this.inDir.getPath()));
        } else {
            PrintWriter outXML = null;
            try {
                outXML = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.outFile.getPath()), "windows-1251"));
                outXML.write("<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n");
                outXML.write("<operations>\n");
                compileOperations(this.inDir.getPath(), outXML);
                outXML.write("</operations>\n");
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            } finally {
                outXML.close();
            }
        }
    }
}
