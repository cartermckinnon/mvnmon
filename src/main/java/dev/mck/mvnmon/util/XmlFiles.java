package dev.mck.mvnmon.util;

import de.pdark.decentxml.Attribute;
import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.XMLParser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum XmlFiles {
  INSTANCE;

  public static List<Element> findElementsWithName(Element rootElement, String elementName) {
    List<Element> answer = new ArrayList<>();
    List<Element> children = rootElement.getChildren();
    for (Element child : children) {
      if (Objects.equals(elementName, child.getName())) {
        answer.add(child);
      } else {
        answer.addAll(findElementsWithName(child, elementName));
      }
    }
    return answer;
  }

  public static String firstChildTextContent(Element element, String elementName) {
    Element child = firstChild(element, elementName);
    if (child != null) {
      return child.getText();
    }
    return null;
  }

  public static Element firstChild(Element element, String elementName) {
    return element.getChild(elementName);
  }

  public static Document parseXmlFile(URL pomFileUrl) throws IOException {
    byte[] bytes = pomFileUrl.openStream().readAllBytes();
    String xml = new String(bytes, StandardCharsets.UTF_8);
    return XMLParser.parse(xml);
  }

  public static Document parseXmlFile(File pomFile) throws IOException {
    return XMLParser.parse(pomFile);
  }

  public static Document parseXmlFile(String xml) {
    return XMLParser.parse(xml);
  }

  public static boolean updateFirstChild(Element parentElement, String elementName, String value) {
    if (parentElement != null) {
      Element element = firstChild(parentElement, elementName);
      if (element != null) {
        String textContent = element.getText();
        if (textContent == null || !value.equals(textContent)) {
          element.setText(value);
          return true;
        }
      }
    }
    return false;
  }

  public static boolean updateFirstChildIgnoringIfAttribute(
      Element parentElement,
      String elementName,
      String value,
      String attributeName,
      String attributeValue) {
    if (parentElement != null) {
      Element element = firstChild(parentElement, elementName);
      if (element != null) {
        if (attributeName != null && attributeValue != null) {
          Attribute attr = element.getAttribute(attributeName);
          if (attr != null && attributeValue.equals(attr.getValue())) {
            return false;
          }
        }
        String textContent = element.getText();
        if (textContent == null || !value.equals(textContent)) {
          element.setText(value);
          return true;
        }
      }
    }
    return false;
  }
}
