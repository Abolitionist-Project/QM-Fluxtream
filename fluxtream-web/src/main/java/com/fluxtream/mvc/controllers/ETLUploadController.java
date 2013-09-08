package com.fluxtream.mvc.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import com.fluxtream.auth.AuthHelper;
import com.fluxtream.services.DataService;
import com.fluxtream.services.GuestService;
import com.quantimodo.etl.ETL;
import com.quantimodo.etl.QuantimodoRecord;
import com.quantimodo.etl.HTMLUtil;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

@Controller
public class ETLUploadController {
    Logger logger = Logger.getLogger(ETLUploadController.class);

    @Autowired
    private GuestService guestService;

    @Autowired
    private DataService dataService;

    @ResponseBody
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String handleUpload(final @RequestParam("files[]") ArrayList<MultipartFile> files,
                               final HttpServletRequest httpServletRequest) {
        //httpServletRequest.getPrincipal();
        StringBuilder result = new StringBuilder();
	final ETL etl = new ETL();
	final long userID = AuthHelper.getGuestId();

        for (final MultipartFile file : files) {
            final String filename = file.getOriginalFilename();
            try {
                final File tempFile = File.createTempFile("QuantimodoETLUpload-", '.' + getExtension(filename));
                tempFile.deleteOnExit(); // Try to delete even if an error arises
                file.transferTo(tempFile);
                logger.debug(String.format("Uploaded file %s is ready for ETL processing at %s.", filename, tempFile.getCanonicalPath()));

                // Do all ETL processing
                final QuantimodoRecord[] records = etl.handle(tempFile);
                dataService.insert(guestService.getGuestById(AuthHelper.getGuestId()), records);

                tempFile.delete();
                logger.debug(String.format("ETL upload of %s completed.", filename));
                result.append("ETL file upload processed: ");
		result.append(java.util.Arrays.toString(records));
            } catch (final IllegalStateException e) {
                logger.error(String.format("ETL upload of %s failed.", filename), e);
                result.append("ETL file upload failed: ");

		final StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		result.append("<h2>ERROR!</h2><pre>").append(HTMLUtil.escapeHTMLOnly(error.getBuffer().toString())).append("</pre>");
            } catch (final IOException e) {
                logger.error(String.format("ETL upload of %s failed.", filename), e);
                result.append("ETL file upload failed: ");

		final StringWriter error = new StringWriter();
		e.printStackTrace(new PrintWriter(error));
		result.append("<h2>ERROR!</h2><pre>").append(HTMLUtil.escapeHTMLOnly(error.getBuffer().toString())).append("</pre>");
            }
            result.append(filename).append(" (").append(file.getSize()).append("B)").append("<br />\n");
	}
        return result.toString();
    }
    
    private static final String getExtension(final String filename) {
	if (filename == null) return null;
	final String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
	final int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
	final int dotIndex = afterLastSlash.indexOf('.', afterLastBackslash);
	return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex + 1);
    }
}
