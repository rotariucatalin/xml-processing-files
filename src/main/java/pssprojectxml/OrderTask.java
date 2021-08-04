package pssprojectxml;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OrderTask extends TimerTask {

    @Override
    public void run() {

        //Location where the xml files will appear
        final String LOCATION = "C:/Users/user/Desktop/xml-files";

        LinkedHashMap<String, List<Product>> productHashMap = new LinkedHashMap<>();
        Set<Supplier> suppliers = new HashSet<>();
        StringBuilder orderNumber = new StringBuilder();

        parseXMLFiles(LOCATION, productHashMap, suppliers, orderNumber);
        createFiles(suppliers, LOCATION, productHashMap, orderNumber);
        parseNonXMLFiles(LOCATION);
    }

    //Method that is used to search on the specific directory and then open each xml file
    //After the file is opened parse all the xml nodes to populate the LinkedHashMap which will be used to create separate files for each Supplier
    //After the file is processed it will be moved to BACKUP directory
    private static void parseXMLFiles(String location, LinkedHashMap<String, List<Product>> productHashMap, Set<Supplier> suppliers, StringBuilder orderNumber) {

        try (Stream<Path> paths = Files.walk(Paths.get(location), 1)) {

            //Apply some filter to avoid other files to be parse it
            //First condition is used to filter only the files that have the order number at the end of the filename
            //Second condition is user to filter only the files that have XML extension
            //For each iteration we parse 1 file
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().substring(6,8).matches("[0-9]+"))
                    .filter(path -> path.getFileName().toString().substring(9,12).equals("xml"))
                    .limit(1)
                    .forEach(path -> {

                        //Store the order number to use it when we create the individual files for suppliers
                        orderNumber.append(path.getFileName().toString(), 6, 8);


                        //Create a DocumentBuilderFactory new instance to be able to parse the XML file
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder;
                        Document doc = null;
                        List<Order> orders = new ArrayList<>();

                        try {
                            builder = factory.newDocumentBuilder();
                            doc = builder.parse(String.valueOf(path.toAbsolutePath()));
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                        catch (SAXException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        doc.getDocumentElement().normalize();

                        //First parse all the order nodes
                        NodeList nodeListOrders = doc.getElementsByTagName("order");
                        for (int j = 0; j < nodeListOrders.getLength(); j++) {

                            Node nodeOrder = nodeListOrders.item(j);
                            if(nodeOrder.getNodeType() == Node.ELEMENT_NODE) {
                                Element elemOrder = (Element) nodeOrder;

                                int orderId = Integer.parseInt(elemOrder.getAttributes().getNamedItem("ID").getNodeValue());
                                String dateCreated = elemOrder.getAttributes().getNamedItem("created").getNodeValue();

                                List<Product> products = new ArrayList<>();
                                Order order = new Order();
                                order.setOrderId(orderId);
                                order.setDateCreated(dateCreated);

                                //For current order node get all the product nodes
                                NodeList nodeList = ((Element) nodeOrder).getElementsByTagName("product");
                                for (int i = 0; i < nodeList.getLength(); i++) {
                                    Node node = nodeList.item(i);
                                    if(node.getNodeType() == Node.ELEMENT_NODE) {

                                        Element elem = (Element) node;
                                        String supplierName = elem.getElementsByTagName("supplier").item(0).getTextContent();
                                        String productDescription = elem.getElementsByTagName("description").item(0).getTextContent();
                                        String productGtin = elem.getElementsByTagName("gtin").item(0).getTextContent();
                                        String productCurrency = elem.getElementsByTagName("price").item(0).getAttributes().getNamedItem("currency").getNodeValue();
                                        double productPrice = Double.parseDouble(elem.getElementsByTagName("price").item(0).getTextContent());

                                        Product product = new Product();
                                        Price price = new Price();

                                        product.setDescription(productDescription);
                                        product.setGtin(productGtin);
                                        price.setPrice(productPrice);
                                        price.setCurrency(productCurrency);
                                        product.setPrice(price);
                                        product.setSupplier(supplierName);

                                        products.add(product);
                                    }
                                }

                                //Sort the product list based on the condition in the pojo class
                                Collections.sort(products);

                                order.setProductList(products);
                                orders.add(order);

                            }

                        }

                        //Sort the order list based on the condition in the pojo class
                        Collections.sort(orders);

                        //Parse the order list to create the LinkedHashMap
                        orders.forEach(order -> {

                            order.getProductList().forEach(product -> {

                                String supplierName = product.getSupplier();
                                if(productHashMap.containsKey(supplierName)) {

                                    productHashMap.get(supplierName).add(product);

                                } else {

                                    Supplier supplier = new Supplier();
                                    supplier.setName(supplierName);
                                    suppliers.add(supplier);

                                    List<Product> productList = new ArrayList<>();

                                    productList.add(product);
                                    productHashMap.put(supplier.getName(), productList);

                                }

                            });
                        });

                        //Each XML file is moved to BACKUP directory after is parsed
                        try {

                            Path donePath = Files.createDirectories(Paths.get(path.getParent() + "/backup"));
                            Files.move
                                    (Paths.get(path.toAbsolutePath().toString()),
                                            Paths.get(donePath+"/"+path.getFileName()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Method used to create individual XML file for each supplier
    private static void createFiles(Set<Supplier> suppliers, String location, LinkedHashMap<String, List<Product>> productHashMap, StringBuilder orderNumber) {

        suppliers.forEach(supplier -> {

            List<Product> products = new ArrayList<>();

            //Get the product list for each supplier
            List<Product> productList = productHashMap.get(supplier.getName());
            productList.forEach(product -> {

                Product productNew = new Product();
                productNew.setDescription(product.getDescription());
                productNew.setGtin(product.getGtin());

                Price price = new Price();
                price.setPrice(product.getPrice().getPrice());
                price.setCurrency(product.getPrice().getCurrency());

                productNew.setPrice(price);

                products.add(productNew);

            });

            //XmlMapper used to serialize a POJO class into a XML file
            XmlMapper mapper = new XmlMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            Products productsNew = new Products();
            productsNew.setProduct(products);

            mapper.configure( ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true );
            try {
                Path path = Files.createDirectories(Paths.get(location + "/" + supplier.getName()));
                mapper.writeValue(new File(path+ "/" + supplier.getName()+""+orderNumber.toString()+".xml"), productsNew);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    //Method used to move all the other files to nonXMLFiles
    //Just is created just to have the root location clear
    private static void parseNonXMLFiles(String location) {

        Pattern pattern = Pattern.compile("[0-9]+");

        try (Stream<Path> paths = Files.walk(Paths.get(location), 1)) {

            paths
                    .filter(Files::isRegularFile)
                    .limit(10)
                    .forEach(path -> {

                        Matcher matcher = pattern.matcher(path.getFileName().toString().substring(6,8));
                        if(!(matcher.matches()) || !(path.getFileName().toString().substring(9,12).equals("xml")) ) {

                            try {
                                Path nonXmlPath = Files.createDirectories(Paths.get(path.getParent() + "/nonXMLFiles"));
                                Files.move
                                        (Paths.get(path.toAbsolutePath().toString()),
                                                Paths.get(nonXmlPath+"/"+path.getFileName()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
