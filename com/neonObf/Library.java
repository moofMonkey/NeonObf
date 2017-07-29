package com.neonObf;

import java.io.File;
import java.util.ArrayList;

public class Library {
    public File file;
    public ArrayList<String> classNames;
    public boolean isLibrary;

    public Library(File file, ArrayList<String> classNames, boolean isLibrary) {
        this.file = file;
        this.classNames = classNames;
        this.isLibrary = isLibrary;
    }
}
