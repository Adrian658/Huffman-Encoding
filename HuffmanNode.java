/*
 * Represents a Huffman node that belongs to a Huffman tree
 */
public class HuffmanNode implements Comparable<HuffmanNode> {
  
  /* Represents the character denoted by this node */
  private Character inChar = null;
  
  /* Represents the frequency of all characters contained in this nodes subtree */
  private int frequency = 0;
  
  /* This nodes right child */
  private HuffmanNode right = null;
  
  /* This nodes left child */
  private HuffmanNode left = null;
  
  /* Path from the root node of the Huffman tree to this node in binary
   * Initialized after tree is constructed */
  private String path = "";
  
  /*
   * Constructor for HuffmanNode
   * @param inChar the character this node represents
   * @param frequency the frequency of all character contained in this nodes subtree
   */
  public HuffmanNode(Character inChar, int frequency) {
    this.inChar = inChar;
    this.frequency = frequency;
  }
  
  /*
   * Constructor for HuffmanNode
   * @param inChar the character this node represents
   * @param frequency the frequency of all character contained in this nodes subtree
   * @param left this nodes left child
   * @param right this nodes right child
   */
  public HuffmanNode(Character inChar, int frequency, HuffmanNode left, HuffmanNode right) {
    this.inChar = inChar;
    this.frequency = frequency;
    this.right = right;
    this.left = left;
  }
  
  /* Return frequency of node */
  public int getFrequency() {
    return frequency;
  }
  
  /* Return character of node */
  public char getInChar() {
    return inChar;
  }
  
  /* Return left child of node */
  public HuffmanNode getLeft() {
    return left;
  }
  
  /* Return right child of node */
  public HuffmanNode getRight() {
    return right;
  }
  
  /* Set path of node */
  public void setPath(String path) {
    this.path = path;
  }
  
  /* Return path of node after Huffman tree is generated */
  public String getPath() {
    return path;
  }
  
  /* Returns whether the node is a leaf node(has no children) */
  public boolean isLeafNode() {
    if (inChar == null)
      return false;
    else
      return true;
  }
  
  /* Returns if this node is greater than the specified node */
  public int compareTo(HuffmanNode node) {
    return Integer.compare(this.frequency, node.frequency);
  }
   
}