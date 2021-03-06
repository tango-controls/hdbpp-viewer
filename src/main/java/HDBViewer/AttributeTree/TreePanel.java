/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package HDBViewer.AttributeTree;

import HDBViewer.AttributeInfo;
import HDBViewer.Utils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.tango.jhdb.HdbFailed;
import org.tango.jhdb.HdbReader;
import org.tango.jhdb.HdbSigParam;
import org.tango.jhdb.SignalInfo;
import org.tango.jhdb.data.HdbData;

/**
 *
 * Selection tree for attribute
 */

class RootNode extends TreeNode {

  RootNode(HdbReader reader) {
    this.reader = reader;
  }
  
  @Override
  void populateNode() {

    if(reader==null)
      return;
    
    try {
      String[] hosts = reader.getHosts();
      for (String host : hosts) {
        add(new HostNode(reader, host));
      }
    } catch(HdbFailed e) {
      Utils.showError(e.getMessage());
    }
    
  }
  
  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.hosticon;
  }
  
  @Override
  public String toString() {
    return "Root";
  }

}

class HostNode extends TreeNode {

  String host;
  String alias;
  
  HostNode(HdbReader reader,String host) {
    this.reader = reader;
    this.host = host;
    alias = HostAlias.getInstance().getAliasFor(host);
  }
  
  @Override
  void populateNode() {    
    try {
      String[] domains = reader.getDomains(host);
      for (String domain : domains) {
        add(new DomainNode(reader, host, domain));
      }
    } catch(HdbFailed e) {
      Utils.showError(e.getMessage());
    }
  }
  
  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.hosticon;
  }
  
  @Override
  public String toString() {
    return alias;
  }

}

class DomainNode extends TreeNode {

  String host;
  String domain;

  DomainNode(HdbReader reader,String host,String domain) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
  }
  
  @Override
  void populateNode() {
    try {
      String[] families = reader.getFamilies(host,domain);
      for (String family : families) {
        add(new FamilyNode(reader, host, domain, family));
      }
    } catch(HdbFailed e) {
      Utils.showError(e.getMessage());
    }    
  }
  
  @Override
  public String toString() {
    return domain;
  }

}

class FamilyNode extends TreeNode {

  String host;
  String domain;
  String family;
  
  FamilyNode(HdbReader reader,String host,String domain,String family) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
    this.family = family;
  }
  
  @Override
  void populateNode() {
    try {
      String[] members = reader.getMembers(host,domain,family);
      for (String member : members) {
        add(new MemberNode(reader, host, domain, family, member));
      }
    } catch(HdbFailed e) {
      Utils.showError(e.getMessage());
    }    
  }
  
  @Override
  public String toString() {
    return family;
  }

}

class MemberNode extends TreeNode {

  String host;
  String domain;
  String family;
  String member;
  
  MemberNode(HdbReader reader,String host,String domain,String family,String member) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
    this.family = family;
    this.member = member;
  }
  
  @Override
  void populateNode() {
    try {
      String[] names = reader.getNames(host,domain,family,member);
      for (String name : names) {
        add(new AttributeNode(reader, host, domain, family, member, name));
      }
    } catch(HdbFailed e) {
      Utils.showError(e.getMessage());
    }    
  }
  
  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.devicon;
  }
  
  @Override
  public String toString() {
    return member;
  }

}

class IntervalNode extends TreeNode {

  String host;
  String domain;
  String family;
  String member;
  String name;
  SignalInfo.Interval interval;
  
  IntervalNode(HdbReader reader,String host,String domain,String family,String member,String name,SignalInfo.Interval interval) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
    this.family = family;
    this.member = member;
    this.name = name;
    this.interval = interval;
  }
  
  @Override
  void populateNode() {    
    for(HdbData.Aggregate a : HdbData.Aggregate.values())
      add(new AggregateNode(reader, host, domain, family, member, name, interval, a));
  }
  
  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.aggicon;
  }
  
  @Override
  public String toString() {
    return interval.toString();
  }

}

class AggregateNode extends TreeNode {

  String host;
  String domain;
  String family;
  String member;
  String name;
  SignalInfo.Interval interval;
  HdbData.Aggregate aggregate;
  
  AggregateNode(HdbReader reader,String host,String domain,String family,String member,String name,SignalInfo.Interval interval,HdbData.Aggregate aggregate) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
    this.family = family;
    this.member = member;
    this.name = name;
    this.interval = interval;
    this.aggregate = aggregate;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }
  
  @Override
  void populateNode() {
  }
  
  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.leaficon;
  }

  public String getAttributeName() {
    return domain + "/" + family + "/" + member + "/" + name;
  }
  
  @Override
  public String toString() {
    return aggregate.toString();
  }

}

class AttributeNode extends TreeNode {

  String host;
  String domain;
  String family;
  String member;
  String name;
  
  AttributeNode(HdbReader reader,String host,String domain,String family,String member,String name) {
    this.reader = reader;
    this.host = host;
    this.domain = domain;
    this.family = family;
    this.member = member;
    this.name = name;
  }
  
  @Override
  void populateNode() {
    if(reader.isFeatureSupported(HdbReader.Feature.AGGREGATES)) {
      add(new IntervalNode(reader, host, domain, family, member, name, SignalInfo.Interval.ONE_MIN));
      add(new IntervalNode(reader, host, domain, family, member, name, SignalInfo.Interval.TEN_MIN));
      add(new IntervalNode(reader, host, domain, family, member, name, SignalInfo.Interval.ONE_HOUR));
      add(new IntervalNode(reader, host, domain, family, member, name, SignalInfo.Interval.EIGHT_HOUR));
      add(new IntervalNode(reader, host, domain, family, member, name, SignalInfo.Interval.ONE_DAY));
    }
  }
  
  @Override
  public boolean isLeaf() {
    return !reader.isFeatureSupported(HdbReader.Feature.AGGREGATES);
  }

  @Override
  ImageIcon getIcon() {
    return TreeNodeRenderer.atticon;    
  }
  
  @Override
  public String toString() {
    return name;
  }
    
  public String getAttributeName() {
    return domain + "/" + family + "/" + member + "/" + name;
  }

}

public class TreePanel extends JPanel implements MouseListener,TreeSelectionListener,KeyListener {

  TreeNode root;
  DefaultTreeModel treeModel;
  JTree tree;
  JScrollPane treeView;
  ArrayList<TreeListener> listeners;
  JPopupMenu actionMenu;
  JMenuItem addMenu;
  JMenu addConfMenu;
  JMenuItem addConfAll;
  JMenuItem addConfLabel;
  JMenuItem addConfUnit;
  JMenuItem addConfDisplayUnit;
  JMenuItem addConfStandardUnit;
  JMenuItem addConfFormat;
  JMenuItem addConfArchRelChange;
  JMenuItem addConfArchAbsChange;
  JMenuItem addConfArchPeriod;
  JMenuItem addConfDescription;
                     
  public TreePanel(HdbReader reader) {

    root = new RootNode(reader);
    treeModel = new DefaultTreeModel(root);
    tree = new JTree(treeModel);
    tree.setEditable(false);
    tree.setCellRenderer(new TreeNodeRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    //tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setBorder(BorderFactory.createLoweredBevelBorder());
    tree.addMouseListener(this);
    tree.addTreeSelectionListener(this);
    tree.addKeyListener(this);
    tree.setToggleClickCount(0);
    treeView = new JScrollPane(tree);
    setLayout(new BorderLayout());
    add(treeView, BorderLayout.CENTER);
    setPreferredSize(new Dimension(250,400));
    setMinimumSize(new Dimension(10,10));
    listeners = new ArrayList<>();

    actionMenu = new JPopupMenu();

    addMenu = new JMenuItem("Add");
    actionMenu.add(addMenu);
    addMenu.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_DATA);
      }      
    });

    addConfMenu = new JMenu("Configuration");
    actionMenu.add(addConfMenu);
    
    addConfAll = new JMenuItem("All");
    addConfMenu.add(addConfAll);
    addConfAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_ALL);
      }      
    });
    
    addConfLabel = new JMenuItem("Label");
    addConfMenu.add(addConfLabel);
    addConfLabel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_LABEL);
      }      
    });

    addConfUnit = new JMenuItem("Unit");
    addConfMenu.add(addConfUnit);
    addConfUnit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_UNIT);
      }      
    });

    addConfDisplayUnit = new JMenuItem("Display Unit");
    addConfMenu.add(addConfDisplayUnit);
    addConfDisplayUnit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_DISPLAY_UNIT);
      }      
    });

    addConfStandardUnit = new JMenuItem("Standard Unit");
    addConfMenu.add(addConfStandardUnit);
    addConfStandardUnit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_STANDARD_UNIT);
      }      
    });

    addConfFormat = new JMenuItem("Format");
    addConfMenu.add(addConfFormat);
    addConfFormat.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_FORMAT);
      }      
    });

    addConfArchRelChange = new JMenuItem("Arch Rel Change");
    addConfMenu.add(addConfArchRelChange);
    addConfArchRelChange.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_ARCH_REL_CHANGE);
      }      
    });
    
    addConfArchAbsChange = new JMenuItem("Arch Abs Change");
    addConfMenu.add(addConfArchAbsChange);
    addConfArchAbsChange.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_ARCH_ABS_CHANGE);
      }      
    });

    addConfArchPeriod = new JMenuItem("Arch Period");
    addConfMenu.add(addConfArchPeriod);
    addConfArchPeriod.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_ARCH_PERIOD);
      }      
    });

    addConfDescription = new JMenuItem("Description");
    addConfMenu.add(addConfDescription);
    addConfDescription.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireTreeListener(HdbSigParam.QUERY_CFG_DESCRIPTION);
      }      
    });
    
  }

  public ArrayList<AttributeInfo> getSelection() {
    return getSelection(HdbSigParam.QUERY_DATA);
  }
  
  public ArrayList<AttributeInfo> getSelection(int queryMode) {
    
    ArrayList<AttributeInfo> ret = new ArrayList<>();
    TreePath[] paths = tree.getSelectionPaths();
    
    for(int i=0;i<paths.length;i++) {
      TreeNode node = (TreeNode)paths[i].getLastPathComponent();
      if(node instanceof AttributeNode) {
        AttributeNode aNode = (AttributeNode)node;
        AttributeInfo ai = new AttributeInfo();
        ai.name = aNode.getAttributeName();
        ai.host = aNode.host;
        ai.step = false;
        ai.queryMode = queryMode;        
        ai.interval = SignalInfo.Interval.NONE;
        ret.add(ai);
      }
      if(node instanceof AggregateNode ) {
        AggregateNode aNode = (AggregateNode)node;
        AttributeInfo ai = new AttributeInfo();
        ai.name = aNode.getAttributeName();
        ai.host = aNode.host;
        ai.step = false;
        ai.queryMode = queryMode;        
        ai.interval = aNode.interval;
        ai.addAggregate(aNode.aggregate);
        ret.add(ai);        
      }
      
    }
    
    return ret;
    
  }
  
  public TreePath getSelectionPath() {
    
    return tree.getSelectionPath();
  
  }
  
  private TreeNode searchNode(TreeNode startNode,String value) {

    int numChild = treeModel.getChildCount(startNode);
    int i = 0;
    boolean found = false;
    TreeNode elem = null;

    while (i < numChild && !found) {
      elem = (TreeNode) treeModel.getChild(startNode, i);
      found = elem.toString().compareToIgnoreCase(value) == 0;
      if (!found) i++;
    }

    if(found) {
      return elem;
    } else {
      return null;
    }

  } 

  public void setSelectionPath(TreePath path) {
    
    if (path != null) {

      // Reselect old node
      TreePath newPath = new TreePath(root);
      TreeNode node = root;
      boolean found = true;
      int i = 1;
      while (found && i < path.getPathCount()) {

        String item = path.getPathComponent(i).toString();

        // Search for item
        node = searchNode(node,item);

        // Construct the new path
        if (node!=null) {
          newPath = newPath.pathByAddingChild(node);
          i++;
        } else {
          found = false;
        }

      }

      tree.setSelectionPath(newPath);
      tree.expandPath(newPath);
      tree.makeVisible(newPath);
      tree.scrollPathToVisible(newPath);

    }
    
  }
  
  public void addTreeListener(TreeListener l) {
    if(!listeners.contains(l))
      listeners.add(l);
  }
  
  public void removeTreeListener(TreeListener l) {
    listeners.remove(l);
  }
  
  public void clearListener() {
    listeners.clear();
  }
  
  private void fireTreeListener(int mode) {
    
    ArrayList<AttributeInfo> la = getSelection(mode);
    for(TreeListener l:listeners)
      l.attributeAction(this,la);
    
  }
  
  @Override
  public void valueChanged(TreeSelectionEvent e) {


  }

  @Override
  public void mousePressed(MouseEvent e) {

    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
    if (selPath != null) {

      if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {


        if( !tree.isSelectionEmpty() ) {
          
          TreeNode node = (TreeNode)selPath.getLastPathComponent();
          
          if (node instanceof AttributeNode || node instanceof AggregateNode) {

            // Check that the node is not already selected
            // If not, add it to the path
            if (!tree.isPathSelected(selPath))
              tree.addSelectionPath(selPath);

            int nbSel = tree.getSelectionCount();
            if (nbSel > 1)
              addMenu.setText("Add " + nbSel + " items");
            else
              addMenu.setText("Add");

            actionMenu.show(tree, e.getX(), e.getY());

          }

        }
      }
      
      if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {
        // Force single selection on double click
        tree.setSelectionPath(selPath);
        fireTreeListener(HdbSigParam.QUERY_DATA);
      }

    }

  }
  @Override
  public void mouseClicked(MouseEvent e) {}
  @Override
  public void mouseReleased(MouseEvent e) {}
  @Override
  public void mouseEntered(MouseEvent e) {}
  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode()==KeyEvent.VK_ENTER) {
        fireTreeListener(HdbSigParam.QUERY_DATA);      
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
    
}
