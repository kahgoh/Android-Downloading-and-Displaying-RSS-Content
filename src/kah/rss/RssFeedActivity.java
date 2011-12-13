package kah.rss;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kah.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

/**
 * A simple activity that demonstrates how to parse RSS content and display the
 * latest entry in a {@link WebView}.
 * 
 * @author Kah
 */
public class RssFeedActivity extends Activity {
	/**
	 * The URL of the RSS source.
	 */
	private static final String source = "https://www.linux.com/rss/feeds.php";

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStart() {
		super.onStart();

		// Downloading the RSS feed needs to be done on a separate thread.
		Thread downloadThread = new Thread(new Runnable() {

			public void run() {
				try {
					updateView(getLatestContent(retrieveRssDocument()));
				} catch (Exception e) {
					Log.e("Content Retriever", e.getLocalizedMessage(), e);
				}
			}
		});

		downloadThread.start();
	}

	/**
	 * Updates the {@link WebView} with the provided content.
	 * 
	 * @param content
	 *            the content to provide to the {@link WebView}.
	 */
	private void updateView(final String content) {
		final WebView view = (WebView) findViewById(R.id.content);
		view.loadData("<html><body>" + content + "</body></html>",
				"text/html", null);
	}
	
	/**
	 * Download the content and parse into a {@link Document}.
	 * 
	 * @return a {@link Document} containing the RSS data
	 * 
	 * @throws IOException
	 *             if an io error was encountered
	 * @throws ParserConfigurationException
	 *             an error was encountered while trying to build the document
	 * @throws SAXException
	 *             an error was encountered while trying to parse the RSS
	 *             content
	 */
	private Document retrieveRssDocument() throws IOException,
			ParserConfigurationException, SAXException {

		URL url = new URL(source);
		URLConnection connection = url.openConnection();
		InputStream inStream = connection.getInputStream();

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			return builder.parse(new BufferedInputStream(inStream));
		} finally {
			inStream.close();
		}
	}

	/**
	 * Obtains the content from the latest RSS entry.
	 * 
	 * @return the content from the latest RSS entry
	 * 
	 * @throws IOException
	 *             if an io error was encountered
	 * @throws ParserConfigurationException
	 *             an error was encountered while trying to build the document
	 * @throws SAXException
	 *             an error was encountered while trying to parse the RSS
	 *             content
	 */
	private String getLatestContent(Document document) throws IOException,
			ParserConfigurationException, SAXException {

		NodeList nodeList = document.getElementsByTagName("item");
		int length = nodeList.getLength();
		if (length > 0) {
			return getDescriptionContent(nodeList.item(0));
		}

		return null;
	}

	/**
	 * Searches for the first "description" node from the provided root.
	 * 
	 * @param root
	 *            the root node to start the search from
	 * @return the first "description" node that was found; {@code null} is
	 *         returned if no match is found
	 */
	private String getDescriptionContent(Node root) {
		if (root instanceof Element) {
			Element asElement = (Element) root;
			if (asElement.getTagName().equalsIgnoreCase("description")) {
				return asElement.getTextContent();
			}
		}

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			String result = getDescriptionContent(node);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}