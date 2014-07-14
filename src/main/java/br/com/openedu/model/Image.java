package br.com.openedu.model;

import java.io.InputStream;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

public class Image extends GridFSInputFile {

	public Image(GridFS gridFS, InputStream inputStream, String filename) {
		super(gridFS, inputStream, filename);
	}
}

