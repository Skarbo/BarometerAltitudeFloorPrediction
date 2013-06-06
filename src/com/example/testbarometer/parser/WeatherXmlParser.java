package com.example.testbarometer.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.testbarometer.model.WeatherEntries;
import com.example.testbarometer.model.WeatherEntry;

public class WeatherXmlParser {

	public WeatherEntries parse(InputStream in) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(in);

		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "weatherdata/product/time[@from=@to]";
		NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

		WeatherEntries WeatherEntries = new WeatherEntries();

		if (nodes != null && nodes.getLength() > 0) {
			// nodes.getLength()
			for (int i = 0; i < 10; i++) {
				WeatherEntry weatherEntry = new WeatherEntry();
				Node timeNode = nodes.item(i);
				weatherEntry.from = WeatherEntry
						.parseDate(timeNode.getAttributes().getNamedItem("from").getNodeValue());
				weatherEntry.to = WeatherEntry.parseDate(timeNode.getAttributes().getNamedItem("to").getNodeValue());
				for (int j = 0; j < timeNode.getChildNodes().getLength(); j++) {
					Node locationNode = timeNode.getChildNodes().item(j);
					if (locationNode.getNodeType() == Node.ELEMENT_NODE
							&& locationNode.getNodeName().equalsIgnoreCase("location")) {
						if (WeatherEntries.altitude == 0.0)
							WeatherEntries.altitude = Double.parseDouble(locationNode.getAttributes()
									.getNamedItem("altitude").getNodeValue());
						if (WeatherEntries.latitude == 0.0f)
							WeatherEntries.latitude = Float.parseFloat(locationNode.getAttributes()
									.getNamedItem("latitude").getNodeValue());
						if (WeatherEntries.longitude == 0.0f)
							WeatherEntries.longitude = Float.parseFloat(locationNode.getAttributes()
									.getNamedItem("longitude").getNodeValue());
						for (int k = 0; k < locationNode.getChildNodes().getLength(); k++) {
							Node node = locationNode.getChildNodes().item(k);
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if (node.getNodeName().equalsIgnoreCase("temperature")) {
									weatherEntry.temperature = Double.parseDouble(node.getAttributes()
											.getNamedItem("value").getNodeValue());
								} else if (node.getNodeName().equalsIgnoreCase("pressure")) {
									weatherEntry.pressure = Double.parseDouble(node.getAttributes()
											.getNamedItem("value").getNodeValue());
								}
							}
						}
					}
				}
				WeatherEntries.add(weatherEntry);
			}
		}
		return WeatherEntries;

	}

}