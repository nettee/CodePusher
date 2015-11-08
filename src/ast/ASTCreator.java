package ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * create ASTs for all the java files under certain project
 * <p>
 * The <code>iterator()</code> method returns iterator of ASTs. Iterate through
 * it to get all the ASTs under the project. The iterator is lazy, that is, the
 * AST is not created until you call the <code>next()</code> method.
 *
 */
public class ASTCreator implements Iterator<Tree> {

	private static Logger logger = Logger.getLogger(ASTCreator.class);

	private String[] classpathEntries;
	private String[] sourcepathEntries;

	private List<String> filepaths;
	private Iterator<String> iter;

	public ASTCreator(String projectDirPath) {
		PathExplorer explorer = PathExplorer.startExplore(projectDirPath);
		classpathEntries = explorer.getClassPaths();
		sourcepathEntries = explorer.getSourcePaths();
		filepaths = explorer.getFilePaths();
		iter = filepaths.iterator();
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Tree next() {
		String filepath = iter.next();
		String filename = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
		ASTNode root = createAST(filepath);
		return new Tree(filename, root);
	}

	private ASTNode createAST(String filepath) {

		String program;

		try {
			program = readFromFile(filepath);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read from file " + filepath);
		}

		ASTParser parser = ASTParser.newParser(AST.JLS8);

		parser.setSource(program.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(classpathEntries, sourcepathEntries, null, true);
		parser.setUnitName(filepath);
		parser.setResolveBindings(true);

		logger.info("Create AST for " + filepath);
		return parser.createAST(null);
	}

	private String readFromFile(String path) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(path));
		StringBuilder sb = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while (true) {
			String line = in.readLine();
			if (line == null) {
				break;
			}
			sb.append(line);
			sb.append(ls);
		}
		in.close();
		return sb.toString();
	}

}

/**
 * get:
 * <li>all java files' filenames and paths</li>
 * <li>all the source directories's paths</li>
 * <li>all the binary files' paths</li> in a project
 *
 */
class PathExplorer {

	private List<String> filePaths = new ArrayList<String>();
	private List<String> srcs = new ArrayList<String>();
	private List<String> bins = new ArrayList<String>();

	public static PathExplorer startExplore(String dirPath) {
		return new PathExplorer(dirPath);
	}

	private PathExplorer(String dirPath) {
		readDirectory(dirPath);
	}

	// recursively read all files under dirPath
	private void readDirectory(String dirPath) {

		File dir = new File(dirPath);

		if (!dir.exists()) {
			throw new IllegalStateException("Illegal Directory Path: " + dir.getAbsolutePath());
		}

		for (File subdir : dir.listFiles()) {
			if (subdir.isDirectory()) {
				if (subdir.getName().trim().equals("bin")) {
					bins.add(subdir.getAbsolutePath());
				} else if (subdir.getName().startsWith("src")) {
					srcs.add(subdir.getAbsolutePath());
				}
				// recursively read subdirectories except bin/
				if (!subdir.getName().startsWith("bin")) {
					readDirectory(subdir.getPath());
				}
			} else {
				if (subdir.getName().endsWith(".java")) {
					filePaths.add(subdir.getAbsolutePath());
				} else if (subdir.getName().endsWith(".jar")) {
					bins.add(subdir.getAbsolutePath());
				}
			}
		}
	}

	public List<String> getFilePaths() {
		return filePaths;
	}

	public String[] getSourcePaths() {
		return srcs.toArray(new String[srcs.size()]);
	}

	public String[] getClassPaths() {
		return bins.toArray(new String[bins.size()]);
	}
}
