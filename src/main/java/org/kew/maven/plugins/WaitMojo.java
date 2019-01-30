package org.kew.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

@Mojo(name = "wait-http")
public class WaitMojo extends AbstractMojo {
	@Parameter(property="protocol", defaultValue="http")
	String protocol;

	@Parameter(property="host", defaultValue="localhost")
	String host;

	@Parameter(property="port", defaultValue="8080")
	int port;

	@Parameter(property="file", defaultValue="/")
	String file;

	@Parameter(property="username", defaultValue="")
	String username;

	@Parameter(property="password", defaultValue="")
	String password;

	@Parameter(property="timeout", defaultValue="30000")
	int timeout;

	@Parameter(property="maxcount", defaultValue="0")
	int maxcount;

	@Parameter(property="skip", defaultValue="false")
	boolean skip;

	@Parameter(property="read", defaultValue="false")
	boolean read;
	
	@Parameter(property="initialWait", defaultValue="0")
	int initialWait;

	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Skipped waiting for " + protocol + "://" + host);
			return;
		}

		getLog().info("Protocol: " + protocol);
		getLog().info("Host: " + host);
		getLog().info("Port: " + port);
		getLog().info("File: " + file);
		getLog().info("Basic auth: " + enableBasicAuthentication());
		URL url = getURL();
		int count = maxcount;
		int trials = 1;
		getLog().info("(timeout: " + timeout + " maxcount: " + maxcount + ")");

		if(initialWait > 0) {
			getLog().info("Waiting for " + initialWait + "s before trying to connect");
			try {
				Thread.sleep(initialWait);
			} catch (InterruptedException e1) { // do nothing
			}
		}
		// try to connect
		while (true) {
			try {
				getLog().info(trials + ": Trying to connect to " + url);

				if (enableBasicAuthentication()){
					Authenticator.setDefault (new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password.toCharArray());
						}
					});
				}

				// obtain the connection
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(timeout);
				InputStream stream = connection.getInputStream();

				// if http connection, make sure it gives a 200 response
				if (protocol.equals("http")) {
					HttpURLConnection httpConnection = (HttpURLConnection)connection;
					if(httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						throw new IOException("Connection returned status code: " + httpConnection.getResponseCode());
					}
					getLog().info("Connection returned " + httpConnection.getResponseCode());
				}

				// if read is required, read everything from URL
				if (read) {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String inputLine;

					while ((inputLine = in.readLine()) != null) {
						getLog().debug(inputLine);
					}

					in.close();
				}

				getLog().info("Success: - reached " + url);
				stream.close();
				break;
			}
			catch (IOException e) {
				if (count > 1) {
					count--;
				}
				else if (count != 0) {
					getLog().warn("Cannot connect to " + url, e);
					throw new MojoExecutionException("Cannot connect to " + url, e);
				}
				try {
					Thread.sleep(timeout);
				}
				catch (InterruptedException e1) { // do nothing
				}

				trials++;
			}
		}
	}

	/**
	 * Construct the URL to connect to
	 *
	 * @return the well-formed URL
	 * @throws MojoExecutionException in case of malformed URL
	 */
	public URL getURL() throws MojoExecutionException {
		try {
			return new URL(protocol, host, port, file);
		}
		catch (MalformedURLException e) {
			throw new MojoExecutionException(
					protocol + ", " + host + ", " + port + ", " + file + ": cannot create URL", e);
		}
	}

	/**
	 * Enable basic auth headers or not
	 * @return true if either pwd or username set
	 */
	private boolean enableBasicAuthentication() {
		return !"".equals(username) || !"".equals(password);
	}
}
