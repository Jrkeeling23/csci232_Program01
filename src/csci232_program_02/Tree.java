package csci232_program_02;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

/**
 * 
 * @author (Robert Lafore. 2002. Data Structures and Algorithms in Java (2 ed.). Sams, Indianapolis, IN, USA
 */
public class Tree {
private Node root;                 // first Node of Tree
	
	public Tree() {                    // constructor
		root = null;                   // no nodes in tree yet
	}
	
	
	public Node find(int key) {      // find node with given key
		Node current = root;         // (assumes non-empty tree)
		while (current.key != key) {          // while no match
			if (key < current.key) {          // go left?
				current =  current.leftChild; 
			}
			else {                              // or go right?
				current =  current.rightChild;
			}
			if(current == null)                 // if no child
			{                                   // didn't find it
				return null;              
			}			
		}
		return current;                         // found it
	}  //end find()
	
	/**
	 * Finds the minimum key in the tree simply by going left at 
	 * every opportunity until there are no more nodes
	 * @author Cory Johns
	 * @return the node with the minimum key
	 */
	public Node findMin() {
		Node current = root;
		while (current != null) {
			if (current.leftChild != null) {
				current = current.leftChild;
			}
			else {
				break;
			}
		}
		return current;
	}
	
	/**
	 * Finds the max key in the tree simply by going right at 
	 * every opportunity until there are no more nodes
	 * @author Cory Johns
	 * @return the node with the maximum key
	 */
	public Node findMax() {
		Node current = root;
		while (current != null) {
			if (current.rightChild != null) {
				current = current.rightChild;
			}
			else {
				break;
			}
		}
		return current;
	}
	
	
	public void insert(int id, double dd) {
		insert(root, new Node(id, dd));
	}
	
	/**
	 * Recursively insert the given node
	 * @param local_root
	 * @param insert_node
	 * @return the parent of the inserted node
	 */
	private Node insert(Node local_root, Node insert_node) {
		Node tmp_root = local_root;
		
		if (root == null) {
			root = insert_node;
			tmp_root = root;
		}
		// check if we should go left
		else if (insert_node.key < local_root.key) {
			// check if we should insert the node
			if (local_root.leftChild == null) {
				// insert the node
				local_root.leftChild = insert_node;
				// update height of the inserted node
				local_root.leftChild.update_height();
			}
			else {
				// recur with sub tree (left)
				tmp_root = insert(local_root.leftChild, insert_node);
			}
		}
		// we should go right
		else {
			// check if we should insert the node
			if (local_root.rightChild == null) {
				// insert the node
				local_root.rightChild = insert_node;
				// update height of the inserted node
				local_root.rightChild.update_height();
			}
			else {
				// recur with sub tree (right)
				tmp_root = insert(local_root.rightChild, insert_node);
			}
		}
		// unspooling behavior
		
		// update height of this node
		if (local_root != null) {
			local_root.update_height();
		}
		else {
			root.update_height();
		}
		// return parent of the inserted node
		return tmp_root;
	}
	
	public boolean delete_recur(int key) {
		// start at the root with no parent
		return delete_recur(null, root, key);
	}
	
	private boolean delete_recur(Node parent, Node child, int key) {
		boolean success;
		
		if (child == null) {
			// not in tree
			success = false;
		}
		else if (child.key == key) {
			// found it
			if (parent == null) {
				delete_node(parent, child, false);
			}
			else {
				delete_node(parent, child, (parent.key > child.key));
			}
			success = true;
		}
		else if (child.key > key) {
			// go left
			success = delete_recur(child, child.leftChild, key);
		}
		else {
			// go right
			success = delete_recur(child, child.rightChild, key);
		}
		// unspooling behavior
		
		// updates the height of the deleted node up to the root
		// in all deletion cases these are the only changed nodes
		// except in 2-child deletion where the in-order-successor and up must also be updated
		// this can't happen in the reconnect recursive function since the stack was made with the old tree structure
		child.update_height();
		
		return success;
	}
	
	/**
	 * Checks the 4 deletion cases for the given node, and does the appropriate one
	 * @param parent of the deletion node
	 * @param deletion
	 * @param isLeft true if the deletion node is left of its parent
	 */
	private void delete_node(Node parent, Node deletion, boolean isLeft) {
		// no children case
		if (deletion.leftChild == null && deletion.rightChild == null) {
			if (parent == null) {
				// TODO there are no nodes in the tree!
				root = null;
			}
			else {
				// reassign parent's reference to this node to null
				parent.setChild(isLeft, null);
			}
		}
		// one child case
		else if (deletion.leftChild == null || deletion.rightChild == null) {
			// cut out node by reassigning its parent to its child
			parent.setChild(isLeft, deletion.getChild(deletion.leftChild != null));
		}
		// two children case
		else {
			// successor parent's left = successor's right
			// successor's right = successor's parent
			Node first_to_fix = reconnect_successor(parent, deletion, isLeft);
			
			// update height along path to the former parent of the in-order-successor
			if (parent == null) {
				update_height_from_node(root, first_to_fix);
			}
			else {
				update_height_from_node(parent, first_to_fix);
			}
		}
	}
	
	/**
	 * Updates the height of each node directly between the to given nodes
	 * @param start
	 * @param end
	 */
	private void update_height_from_node(Node start, Node end) {
		if (end.key != start.key) {
			// have not found it yet
			if (end.key < start.key) {
				// go left
				update_height_from_node(start.leftChild, end);
			}
			else {
				// go right
				update_height_from_node(start.rightChild, end);
			}
		}
		// update the heights while unwinding
		start.update_height();
	}
	
	/**
	 * Finds and reconnects the in order successor given the node that is being deleted.
	 * Also updates the height of all affected nodes.
	 * "reconnects" means:
	 *      successor parent's left = successor's right, 
	 *      successor's right = successor's parent
	 * @param deletion the node being deleted
	 * @return the parent of the in order successor
	 */
	private Node reconnect_successor(Node parent, Node deletion, boolean isLeft) {
		// looks bad, but needed in the recursive method
		return reconnect_successor(parent, parent, deletion, deletion, isLeft);
	}
	
	private Node reconnect_successor(Node parent_of_deletion, Node parent, Node current, Node deletion, boolean isLeft) {
		// defaults to the parent of where we are
		Node tmp = parent;
		
		// go right if we are just starting
		if (current == deletion) {
			tmp = reconnect_successor(parent_of_deletion, current, current.rightChild, deletion, isLeft);
		}
		// continue if we are not at the end
		else if (current.leftChild != null) {
			tmp = reconnect_successor(parent_of_deletion, current, current.leftChild, deletion, isLeft);
		}
		// found the in-order-successor (current)
		else {
			//make connections
			if (current != deletion.rightChild) {
				parent.leftChild = current.rightChild;
				current.rightChild = deletion.rightChild;
				// return the parent of the in-order-successor (already set up)
			}
			
			// set parent's relevant child to the successor
			if (parent_of_deletion != null) {
				parent_of_deletion.setChild(isLeft, current);
			}
			else {
				root = current;
			}
			
			// left successor = left deletion
			current.leftChild = deletion.leftChild;
		}
		
		// return the last node that needs its height updated
		// the update can't happen here since the path is different now 
		// (same nodes, but there order on the stack is inconsistent with the tree now)
		return tmp;
	}
	
	
	public boolean delete(int key) {             // delete node with given key
		Node current = root;		             // (assumes non-empty list)
		Node parent = root;
		boolean isLeftChild = true;

		while (current.key != key) {           // search for Node
			parent = current;
			if (key < current.key) {           // go left?
				isLeftChild = true;
				current =  current.leftChild;
			}
			else {                               // or go right?
				isLeftChild = false;
				current =  current.rightChild;
			}
			if(current == null) {                // end of the line,                             
				return false;                    // didn't find it
			}			
		}
		//found the node to delete

		//if no children, simply delete it
		if (current.leftChild == null && current.rightChild == null) {
			if (current == root) {              // if root,
				root = null;                    // tree is empty
			}
			else if (isLeftChild) {
				parent.leftChild = null;        // disconnect
			}                                   // from parent
			else {
				parent.rightChild = null;
			}
		}
		//if no right child, replace with left subtree
		else if (current.rightChild == null) {  
			if (current == root) {
				root = current.leftChild;
			}
			else if (isLeftChild) {
				parent.leftChild = current.leftChild;
			}			
			else {
				parent.rightChild = current.leftChild;
			}
		}

		//if no left child, replace with right subtree
		else if (current.leftChild == null) {  
			if (current == root) {
				root = current.rightChild;
			}
			else if (isLeftChild) {
				parent.leftChild = current.rightChild;
			}			
			else {
				parent.rightChild = current.rightChild;
			}
		}

		else { // two children, so replace with inorder successor
			   // get successor of node to delete (current)
			Node successor = getSuccessor(current);

			// connect parent of current to successor instead
			if (current == root) {
				root = successor;
			}
			else if (isLeftChild) {
				parent.leftChild = successor;
			}
			else {
				parent.rightChild = successor;
			}

			//connect successor to current's left child
			successor.leftChild = current.leftChild;
		} // end else two children
		// (successor cannot have a left child)
		return true;              // success
	}// end delete()

	

	/**
	 * returns node with next-highest value after delNode, goes right child, then right child's left descendants
	 * THIS IS A LIE!!!! This method also takes care of moving the successor's right child. i.e :
	 *      successor parent's left = successor's right, 
	 *      successor's right = successor's parent
	 * 	
	 * @param delNode the Node to find the successor of
	 * @return the in order successor Node
	 */
	private Node getSuccessor(Node delNode) {
		Node successorParent = delNode;
		Node successor = delNode;
		Node current = delNode.rightChild;        // go to the right child
		while (current != null) {                 // until no more
			successorParent = successor;          // left children
			successor = current;
			current = current.leftChild;
		}

		if (successor != delNode.rightChild) {    // if successor not right child,
			//make connections
			successorParent.leftChild = successor.rightChild;
			successor.rightChild = delNode.rightChild;
		}
		return successor;
	}

	
	// modified for general buffered writer instead of System.out.print
	public void traverse(int traverseType, BufferedWriter writer) throws IOException {
		switch (traverseType) {
		case 1:
			writer.write("\nPreorder traversal: ");
			preOrder(root, writer);
			break;
		case 2:
			writer.write("\nInorder traversal: ");
			inOrder(root, writer);
			break;
		case 3:
			writer.write("\nPostorder traversal: ");
			postOrder(root, writer);
			break;
		default:
			writer.write("Invalid traversal type\n");
			break;
		}
		System.out.println();
	}

	// modified for general buffered writer instead of System.out.print
	private void preOrder(Node localRoot, BufferedWriter writer) throws IOException {
		if (localRoot != null) {
			writer.write(localRoot.key + " ");	
			preOrder(localRoot.leftChild, writer);
			preOrder(localRoot.rightChild, writer);	
		}
	}

	// modified for general buffered writer instead of System.out.print
	private void inOrder(Node localRoot, BufferedWriter writer) throws IOException {
		if (localRoot != null) {
			inOrder(localRoot.leftChild, writer);
			writer.write(localRoot.key + " ");
			inOrder(localRoot.rightChild, writer);		
		}
	}

	// modified for general buffered writer instead of System.out.print
	private void postOrder(Node localRoot, BufferedWriter writer) throws IOException {
		if (localRoot != null) {
			postOrder(localRoot.leftChild, writer);
			postOrder(localRoot.rightChild, writer);
			writer.write(localRoot.key + " ");		
		}
	}

	/**
	 * Displays the tree as text to the given writer.
	 * Uses the Node class's displayNode() method for each node in the tree.
	 * Replaces null nodes with dashes of equal length to the displayNode result.
	 * Has no restrictions on the length of each node representation or the size of the tree.
	 * @param writer
	 * @throws IOException
	 */
	public void display_tree(BufferedWriter writer) throws IOException {
		int tree_height = root.height;
		int node_width = root.displayNode().length();
		
		// slightly backwards way of making a string of length node_width of '-' chars
		String blank = String.format("%0" + node_width + "d", 0).replace('0', '-');
		// and again with ' '
		String filler = String.format("%0" + node_width + "d", 0).replace('0', ' ');
		// again for the separator lines
		String separator = String.format("%0" + node_width + "d", 0).replace('0', '=');
		
		// make array (length = tree_height) of stacks, the index of each stack is its depth from the root
		Stack<String>[] stacks = new Stack[tree_height + 2];
		for (int x=0; x<tree_height + 2; x++) {
			stacks[x] = new Stack<String>();
		}
		
		// recursively traverse (right to left) tree adding node.displayNode() to the stack at index = depth
		display_tree_helper(stacks, blank, root, 0);
		
		// print w/ spacers
		int width = (int) (Math.pow(2, tree_height + 1)) + 1;
		// loop over each tree level (backwards so the root is printed first)
		for (int h=tree_height; h>=0; h--) {
			// length of the spacers on the edge for a given height
			int len_edge = (int) Math.pow(2, h);
			// length of the spacers in the center for a given height
			int len_cen = 0;
			if (h != tree_height) {	// len_cen = 0 for the root level, this expression otherwise
				len_cen = (int) (Math.pow(2, h + 1)) - 1;
			}
			
			// print line separator
			writer.write(print_repeate(separator, width));
			writer.write("\n");
			// print first edge
			writer.write(print_repeate(filler, len_edge));
			// print the nodes
			int num_nodes_at_h = stacks[tree_height- h].size();
			for (int i=0; i<num_nodes_at_h-1; i++) {
				// print a node
				writer.write(stacks[tree_height - h].pop());
				// print the filler
				writer.write(print_repeate(filler, len_cen));
			}
			// print the final node
			writer.write(stacks[tree_height - h].pop());
			// print the ending edge
			writer.write(print_repeate(filler, len_edge));
			// print new line
			writer.write("\n");
		}
		// print line separator
		writer.write(print_repeate(separator, width));
		writer.write("\n");
	}
	
	/**
	 * Prints the given text the given number of times
	 * @param text the string to repeat
	 * @param repeate number of times the string should be repeated
	 * @return the input text repeated the given number of times
	 */
	private String print_repeate(String text, int repeate) {
		String tmp = "";
		for (int i=0; i<repeate; i++) {
			tmp += text;
		}
		return tmp;
	}
	
	/**
	 * Adds each node in the tree to a stack at the correct depth, also adds all null nodes where necessary
	 * @param stacks array of stacks of length root.height + 2
	 * @param blank string representation of a null node
	 * @param local_root the local root node
	 * @param depth the current depth from the root of the tree
	 */
	private void display_tree_helper(Stack<String>[] stacks, String blank, Node local_root, int depth) {
		if (local_root != null) {
			// traverse (right to left)
			display_tree_helper(stacks, blank, local_root.rightChild, depth + 1);
			display_tree_helper(stacks, blank, local_root.leftChild, depth + 1);
			// add the current node to the stack
			stacks[depth].add(local_root.displayNode());
		}
		else {
			// add a blank to represent an empty child
			// must also add all blanks for all the children the empty node could have had
			int max_depth = stacks.length;
			for (int i=0; i<max_depth-depth; i++) {
				for (int k=0; k<(int) Math.pow(2, i); k++) {
					stacks[depth + i].add(blank);
				}
			}
		}
	}
	
	// modified for general buffered writer instead of System.out.print
	public void displayTree(BufferedWriter writer) throws IOException {
		Stack<Node> globalStack = new Stack<Node>();
		globalStack.push(root);
		int nBlanks = 32;
		boolean isRowEmpty = false;
		writer.write(".................................................................\n");
		while (isRowEmpty==false) {
			Stack<Node> localStack = new Stack<Node>();
			isRowEmpty = true;
			
			for (int j = 0; j < nBlanks; j++) {
				writer.write(' ');
			}

			while (globalStack.isEmpty()==false) {
				Node temp = (Node) globalStack.pop();
				if (temp != null) {
					writer.write("" + temp.height);	//TODO changed key to height
					localStack.push(temp.leftChild);
					localStack.push(temp.rightChild);
					if (temp.leftChild != null ||
							temp.rightChild != null) {
						isRowEmpty = false;
					}
				}
				else {
					writer.write("--");
					localStack.push(null);
					localStack.push(null);
				}

				for (int j = 0; j < nBlanks*2-2; j++) {
					writer.write(' ');
				}
			} // end while globalStack not empty
			writer.write("\n");
			nBlanks /= 2;
			while (localStack.isEmpty()==false) {
				globalStack.push(localStack.pop());
			} // end while isRowEmpty is false
			writer.write(".................................................................\n");
		} // end displayTree()
	}
}
