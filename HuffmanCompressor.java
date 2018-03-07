import java.io.*;
import java.util.*;

public class HuffmanCompressor {
  
  /* stores whether all operations of the current encoding were successful */
  private static boolean encodingSuccess = true;
  
  /* stores whether all of the characters from the input file were successfully output in the encoding file */
  private static boolean allCharsCopied = true;
  
  /*
   * Encodes a file using a Huffman encoding generated from a specified file
   * @param inputFileName the name of the file to compress
   * @param encodingFileName the name of the file to generate the Huffman encoding with
   * @param outputFileName the name of the file to output the encoding to
   * @return encodingSuccess if the encoding was successful
   */
  public static String huffmanEncoder(String inputFileName, String encodingFileName, String outputFileName) {
    
    try {
      ArrayList<HuffmanNode> sortedList = findFrequency(encodingFileName); //sorted list of Huffman nodes
      
      HuffmanNode treeNode = generateTree(sortedList); //the Huffman node that is the root of the Huffman tree
      
      if (treeNode.isLeafNode()) { //if the Huffman tree root node is the only node in the tree, generate custom encoding of 0
        treeNode.setPath("0");
        System.out.println(treeNode.getInChar() + ":" + treeNode.getFrequency() + ":" + treeNode.getPath());
      }
      else //if there are multiple nodes in the Huffman tree
        generateListEncoding(treeNode, ""); //generate and print the Huffman tree encoding
      
      encodeFile(inputFileName, outputFileName, treeNode); //Encodes the specified file based on the Huffman coding generated previously
      
      return errorHandling();
      
    }
    catch (IOException e) { //catches any IO Exception and returns it's message
      System.out.println(e.getMessage());
      return e.getMessage();
    }
    
    
  }
  
  /*
   * Finds the frequency of each character in the entered file
   * Generates initial list of Huffman nodes
   * Sorts the list in ascending order of frequency
   * @param fileName the name of the file to be searched
   * @return nodeList the list of sorted Huffman Nodes
   */
  public static ArrayList<HuffmanNode> findFrequency(String fileName) throws IOException {
    InputStream in = new FileInputStream(fileName);
    Reader reader = new InputStreamReader(in);
    int charASCIIvalue; //ASCII value of a character
    /* stores frequencies of characters appearing in the file, stored at their corresponding ASCII index in the array
     * size of extended english ASCII table */
    int[] frequencies = new int[256];
    ArrayList<Integer> extraChars = new ArrayList<>(); //stores any extra characters not contained in the extended ASCII English table
    ArrayList<Integer> extraFreqs = new ArrayList<>(); //stores the corresponding frequencies for any extra characters
    /* Loops through the file one character at a time */
    while ((charASCIIvalue = reader.read()) != -1) {
      try {
        frequencies[charASCIIvalue] = frequencies[charASCIIvalue] + 1; //Increases frequency by one at corresponding ASCII index
      }
      /* ONLY enters if the character is not a part of the extended English ASCII table ***RARELY ENTERS */
      catch (IndexOutOfBoundsException e) {
        boolean loopEntered = false;
        for (int i = 0; i < extraChars.size(); i++) { //Loops across extraChars array
          loopEntered = true;
          if (charASCIIvalue == extraChars.get(i)) { //checking if the character has been encountered before and already exists in the array
            extraFreqs.set(i, extraFreqs.get(i) + 1); //increase frequency by one
          }
        }
        if (!loopEntered) { //if the character has not been encountered, add it to the array
          extraChars.add(charASCIIvalue);
          extraFreqs.add(1);
        }
      }
    }
    
    ArrayList<HuffmanNode> nodeList = new ArrayList<HuffmanNode>(); //list to store the ordered nodes, in ascending order
    /* Loops across the array representing the frequencies of characters that appeared in the file */
    for (int i = 0; i < frequencies.length; i++) {
      if (frequencies[i] != 0) { //if the corresponding character appeared in the file, add it to the new list
        HuffmanNode currentNode = new HuffmanNode((char)i, frequencies[i]);
        nodeList.add(currentNode);
      }
    }
    if (extraChars.size() != 0) { //if there are characters that appeared in the file that are not in the extended English ASCII table
      for (int i = 0; i < extraChars.size(); i++) { //for each item in the list create and add a node to the new list
        HuffmanNode currentNode = new HuffmanNode((char)((int)extraChars.get(i)), extraFreqs.get(i));
        nodeList.add(currentNode);
      }
    }
      
    Collections.sort(nodeList); //Sort the list in ascending order of frequencies
      
    return nodeList;
    
  }
  
  /*
   * WHY I CHOSE ARRAY / ARRAYLIST OVER LINKEDLIST
   * I decided to use the array / ArrayList native lists for a few reasons. 
   * -First was because since I read the file using ASCII values and the extended English ASCII table is a fixed size, I could initialize the array with the size of the table 
   * and insert elements into the array knowing that the elements I was inserting were characters
   * and the ASCI values of those characters were limited to the same number as the size of the array. In the rare case that a character appeared in the file that was not 
   * a part of the extended English ASCII table, I provided a backup method to create a Huffman node for it to, but this is uncommon.
   * -I found myself needing to access elements using indices more often than needing to add or remove elements from the list.
   * -Accessing specific elements is faster and more trivial with array / ArrayList than with LinkedList
   */
  
  /*
   * Merges two HuffmanNodes and returns the combined node
   * @param node1 the first node to be merged
   * @param node2 the second node to be merged
   * @return HuffmanNode the combined node
   */
  public static HuffmanNode mergeNodes(HuffmanNode node1, HuffmanNode node2) {
    
    if (node1.getFrequency() < node2.getFrequency()) //if the first node has a greater frequency than the second node
      return new HuffmanNode(null, node1.getFrequency() + node2.getFrequency(), node1, node2);
    else //if the second node's frequency is greater than or equal to the first node's frequency
      return new HuffmanNode(null, node1.getFrequency() + node2.getFrequency(), node2, node1);
    
  }
  
  /*
   * Generates the Huffman encoding tree for the specified sorted list of Huffman nodes using recursion
   * @param nodeList list of sorted Huffman nodes
   * @return finalNode the root of the final Huffman tree
   */
  public static HuffmanNode generateTree(ArrayList<HuffmanNode> nodeList) {
    
    if (nodeList.size() == 0) //if the list is empty
      return null;
    
    else if (nodeList.size() == 1) //base case when final tree is generated
      return nodeList.get(0);
    
    else {
      /* combine the lowest two frequency nodes to make one combined node with the two nodes as its children */
      HuffmanNode combinedNode = mergeNodes(nodeList.get(0), nodeList.get(1));
      /* remove the nodes we previously combined */
      nodeList.remove(0);
      nodeList.remove(0);
      boolean nodeAdded = false; //to keep track of whether the node was inserted
      /* loops over the whole node list */
      for (int i = 0; i < nodeList.size(); i++) {
        /* searches for where to place the combined node to keep the list in ascending order
         * only enters when the combined node has not been added to the list already */
        if (combinedNode.getFrequency() < nodeList.get(i).getFrequency() && !nodeAdded) {
          nodeList.add(i, combinedNode);
          nodeAdded = true;
        }
      }
      if (!nodeAdded) //if the node was not added, ie. if the node is the largest of the list, add it to the end of the list
        nodeList.add(combinedNode);
      
      HuffmanNode finalNode = generateTree(nodeList); //keep combining the nodes in the list until there is only one node in the list, the root node
      
      return finalNode;
    }
    
  }
  
  /*
   * Lists the Huffman encoding specified by the tree
   * @param rootNode the root node representing the root of a Huffman subtree
   * @param path the path in binary from the root node to a certain node
   */
  public static void generateListEncoding(HuffmanNode rootNode, String path) {

    if (rootNode.isLeafNode()) { //if the node being checked is a leaf node
      rootNode.setPath(path);
      System.out.println(rootNode.getInChar() + ":" + rootNode.getFrequency() + ":" + rootNode.getPath()); //print out info about node
    }
    else { //if the node is an interior node, perform recursion on it's children until a leaf node is found
      generateListEncoding(rootNode.getLeft(), path + "0"); //when going to a nodes left child, 0 is added to the path
      generateListEncoding(rootNode.getRight(), path + "1"); //when going to a nodes right child, 1 is added to the path
    }
    
  }
  
  /*
   * Encodes the specified file using the huffman coding generated
   * Outputs the encoding in the specified file. If file does not exist it is created
   * @param inputFileName name of file to encode
   * @param outputFileName name of file to print encoding into
   * @param rootNode the root of the Huffman tree generated by previous helper methods
   */
  public static void encodeFile(String inputFileName, String outputFileName, HuffmanNode rootNode) throws IOException {
    
    LinkedList<HuffmanNode> leafNodeList = findLeafNodes(rootNode, new LinkedList<HuffmanNode>());
    /* Opens the file to be read from */
    InputStream in = new FileInputStream(inputFileName);
    Reader reader = new InputStreamReader(in);
    /* Checks if the specified file is empty */
    try {
      /* Opens the file to add the encoding to, or if it does not exist creates a new file with the specified name */
      InputStream in2 = new FileInputStream(outputFileName);
      Reader reader2 = new InputStreamReader(in2);
      if (reader2.read() != -1) { //if the first char is null ie. if it is not empty
        System.out.println("The file you entered as the output file is not empty. Please specify an empty file or the name of a new file");
        changeEncodingSuccess();
        return;
      }
    }
    catch (FileNotFoundException e) { } //if the file specified does not exist
    
    FileWriter outputFile = new FileWriter(outputFileName, true);
    int charASCIIvalue; //ASCII value of a character
    int inputDocBits = 0; //counts the number of bits the input doc occupies, assuming each character takes 8 bits of space
    int outputDocBits = 0; //counts the number of bits the output doc occupies
      
    /* Iterates over all the characters of the input file one at a time */
    while ((charASCIIvalue = reader.read()) != -1) {
      inputDocBits = inputDocBits + 8;
      char currentChar = (char)charASCIIvalue; //current character being iterated over
      String path = findLeafNodePath(leafNodeList, currentChar);
      outputFile.write(path);
      outputDocBits = outputDocBits + path.length();
    }
    outputFile.close(); //closes the file that now contains the Huffman encoding of the original file
      
    //Deal with storage
    System.out.println("Input File Size: " + inputDocBits + " bits");
    System.out.println("Output File Size: " + outputDocBits + " bits");
    System.out.println("Savings: " + (inputDocBits - outputDocBits) + " bits");
    
  }
  
  /*
   * Finds the leeaf nodes associated with a specified Huffman tree root node
   * @param rootNode the root node of the Huffman tree
   * @param leafNodes the list of leaf nodes contained by the root node
   * @return leafNodes the list of leaf nodes contained by the root node
   */
  public static LinkedList<HuffmanNode> findLeafNodes(HuffmanNode rootNode, LinkedList<HuffmanNode> leafNodes) {
     
    if (rootNode.isLeafNode()) //if the node being checked is a leaf node add it to the list
        leafNodes.add(rootNode);
    else { //search for leaf nodes in this nodes children
       findLeafNodes(rootNode.getLeft(), leafNodes);
       findLeafNodes(rootNode.getRight(), leafNodes);
    }
     
    return leafNodes;
     
  }
  
  /*
   * Finds the HuffmanNode that corresponds to a specified character
   * @param nodeList the list of leaf nodes contained by the Huffman tree created
   * @param character the character to match to a Huffman node
   * @return String the path from the root node to the node that matches the specified character
   */
  public static String findLeafNodePath(LinkedList<HuffmanNode> nodeList, char character) {
    
    for (HuffmanNode node : nodeList) { //iterates over the list of leaf nodes
      if (node.getInChar() == character) //finds the node that corresponds to the specified character
        return node.getPath();
    }
    
    /* if a character is specified that does not match any HuffmanNode previously created
     * Inform the user that this character is not being added to the output file */
    setAllCharsCopied(false);
    return "";
    
  }
  
  /*
   * Handles any possible errors generated in the encoding process that do not stop the program and informs the user if any error was generated
   */
  public static String errorHandling() {
    
    /* If the input file contained some charatcers that the encoding file did not, 
     * these charatcers did not have Huffman encoding generated so could not be added to the output file */
    if (!getAllCharsCopied())
      System.out.println("Your input file contained some characters the encoding file did not so these characters were not included in the output encoding");
    /* Determines if the encoding process was sucessful in all of it's operations */
    if (getEncodingSuccess()) {
      System.out.println("Encoding Successful");
      return "Encoding Successful";
    }
    else {
      changeEncodingSuccess(); 
      System.out.println("Something went wrong, refer to error message above");       
      return "Something went wrong, refer to error message above";
    }
    
  }
  
  /*
   * Main method
   * Usage: java HuffmanCompressor 'inputFileName' 'encodingFileName' 'outputFileName'
   */
  public static void main(String[] args) {
    
    try {
      HuffmanCompressor.huffmanEncoder(args[0], args[1], args[2]);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: java HuffmanCompressor 'inputFileName' 'encodingFileName' 'outputFileName'");
    }
    
  }
  
  /*
   * Returns whether the encoding was successful in all of it's operations
   */
  public static boolean getEncodingSuccess() {
    return encodingSuccess;
  }
  
  /*
   * Changes status of encoding success
   */
  public static void changeEncodingSuccess() {
    encodingSuccess = !encodingSuccess;
  }
  
  /*
   * Returns allCharsCopied variable
   */
  public static boolean getAllCharsCopied() {
    return allCharsCopied;
  }
  
  /*
   * Changes the value of allCharsCopied
   */
  public static void setAllCharsCopied(boolean allCharsCopied) {
    HuffmanCompressor.allCharsCopied = allCharsCopied;
  }
  
}