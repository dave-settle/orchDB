/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.storage;

/**
 * Class to handle storage of files related to the orchestra - e.g. programme
 * files. 
 * For security, only the file name from all requests (e.g. "c" from "/a/b/c") is used.
 * TODO: support sub-directories securely, if required
 * @author dave.settle@osinet.co.uk on 1 Jun 2023
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service("fileStorageService")
public class FileStorageService implements InitializingBean {
    
    @Value("${bso.file.upload-dir:uploads}")
    private String uploadDirectory;
    
    private Path fileStorageLocation;

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    /**
     * Setup the service and create the storage directory if necessary
     * @throws java.io.IOException if the file upload directory does not exist and cannot be created
     */
    @Override
    public void afterPropertiesSet() throws IOException 
    {
        fileStorageLocation = Paths.get(uploadDirectory);
        log.debug("upload location = [" + fileStorageLocation + "]");
        /*
         * Create directory if not present
         */
        if(!Files.isDirectory(fileStorageLocation)) {
            log.info("creating file storage location " + fileStorageLocation);
            File f = new File(fileStorageLocation.toString());
            f.mkdirs();
        }
    }
    /**
     * Ensure that only files within the upload directory can be accessed
     * @param name the name requested
     * @return the relatative name in the upload directory
     */
    protected Path safePath(String name) {
        Path request = Paths.get(name);
        Path target = fileStorageLocation.resolve(request.getFileName());
        //log.debug("mapped [" + name + "] to [" + target + "]");
        return target;
    }
    /**
     * Store a file with the given filename in the upload directory
     * @param file the file to be uploaded
     * @param name the filename to assign 
     * @throws java.io.IOException 
     */
    public void storeFile(InputStreamSource file, String name) throws IOException 
    {
        Path target = safePath(name);
        log.info("writing file " + target);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
    }
    /**
     * Retrieve a file with the given name from the upload directory
     * @param name
     * @return 
     */
    public Resource getFile(String name) 
    {
        Path target = safePath(name);
        log.info("reading file " + target);
        return new FileSystemResource(target);
    }
    /**
     * Check whether a specific file can be read
     * @param name
     * @return 
     */
    public boolean exists(String name) {
        Path target = safePath(name);
        Resource r = new FileSystemResource(target);
        //log.debug("target [" + name + "] under [" + fileStorageLocation + "] maps to [" + r + "]: readable = " + r.isReadable());
        return r.isReadable();
    }
}
