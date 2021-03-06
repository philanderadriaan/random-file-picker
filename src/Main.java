import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class Main
{

  /**
   * Properties file name.
   */
  private static final String PROP_FILE_NAME = "config.properties";

  /**
   * Source folder key in properties file.
   */
  private static final String SRC_KEY = "source";

  /**
   * Destination folder key in properties file.
   */
  private static final String DEST_KEY = "destination";

  /**
   * Properties object.
   */
  private static final Properties PROP = new Properties();

  /**
   * Folder path delimiter.
   */
  private static final String DELIMITER = ",";

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    try
    {
      log("JAVA_HOME=" + System.getProperty("java.home"));

      // Set look and feel to system.
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      // Load properties file if exists.
      try
      {
        PROP.load(new FileInputStream(PROP_FILE_NAME));
      }
      catch (Exception e)
      {
        log(e.getMessage());
      }
      // Set source and destination folder.
      File srcFolder = getFolder(SRC_KEY, true);
      File destFolder = getFolder(DEST_KEY, false);

      // Clear off destination folder.
      for (File destObj : destFolder.listFiles())
      {
        log("Deleting " + destObj.getAbsolutePath());
        destObj.delete();
      }

      // Check for subfolders.
      File[] srcSubFolders = srcFolder.listFiles();
      if (srcSubFolders.length == 0)
      {
        // Exit if no subfolder.
        log(srcFolder.getAbsolutePath() + " is empty.");
        System.exit(0);
      }

      // Choose a subfolder randomly.
      File srcSubFolder = null;
      srcSubFolder = srcSubFolders[new Random().nextInt(srcSubFolders.length)];
      if (!srcSubFolder.isDirectory())
      {
        // Exit if chosen subfolder is invalid.
        log(srcSubFolder.getAbsolutePath() + " is invalid.");
        System.exit(0);
      }

      // Search files/folders from selected subfolder.
      for (File srcGroup : srcSubFolder.listFiles())
      {
        if (srcGroup.isFile())
        {
          // Copy file directly to destination.
          log("Copying " + srcGroup.getAbsolutePath() + " -> " + destFolder.getAbsolutePath());
          Files.copy(srcGroup.toPath(),
                     Paths.get(destFolder.getAbsolutePath() + "\\" + srcGroup.getName()));
        }
        else if (srcGroup.isDirectory())
        {
          // Copy a random file from group folder to destination.
          File[] srcFiles = srcGroup.listFiles();
          File srcFile = srcFiles[new Random().nextInt(srcFiles.length)];
          log("Copying " + srcFile.getAbsolutePath() + " -> " + destFolder.getAbsolutePath());
          Files.copy(srcFile.toPath(),
                     Paths.get(destFolder.getAbsolutePath() + "\\" + srcFile.getName()));

          // Shuffle group folder.
          File newSrcGrp =
              new File(srcSubFolder.getAbsolutePath() + "\\" +
                       (char) (new Random().nextInt(26) + 'A') + System.currentTimeMillis());
          log("Renaming " + srcGroup.getAbsolutePath() + " -> " + newSrcGrp.getAbsolutePath());
          srcGroup.renameTo(newSrcGrp);
          Thread.sleep(2);
        }
      }

      // Shuffle source subfolder.
      File newSrcSubDir =
          new File(srcFolder.getAbsolutePath() + "\\" + Instant.now().getEpochSecond());
      log("Renaming " + srcSubFolder.getAbsolutePath() + " -> " +
          newSrcSubDir.getAbsolutePath());
      srcSubFolder.renameTo(newSrcSubDir);

      // Save properties file.
      PROP.store(new FileOutputStream(PROP_FILE_NAME), null);
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(),
                                    JOptionPane.ERROR_MESSAGE);
      throw e;
    }
  }

  /**
   * Get folders from properties with exception handling. Use this!
   * 
   * @param key
   * @param multi
   * @return
   * @throws FileNotFoundException
   */
  private static File getFolder(String key, boolean multi) throws FileNotFoundException
  {
    try
    {
      // Get folder from existing properties.
      return getFolder(key);
    }
    catch (Exception e)
    {
      // Lets user choose folders.
      log(e.getMessage());
      setFolder(key, multi);
      return getFolder(key);
    }

  }

  /**
   * Get folders from properties.
   * 
   * @param key
   * @return
   * @throws FileNotFoundException
   */
  private static File getFolder(String key) throws FileNotFoundException
  {
    // Separate folders from properties by delimiter.
    String[] folderNames = ((String) PROP.get(key)).split(DELIMITER);
    List<File> folders = new ArrayList<File>();
    for (String folderName : folderNames)
    {
      File folder = new File(folderName);
      if (folder.isDirectory())
      {
        folders.add(folder);
      }
      else
      {
        throw new FileNotFoundException("Folder not selected.");
      }
    }

    // Returns a random folder from a list of folder.
    return folders.get(new Random().nextInt(folders.size()));
  }

  /**
   * Adds folders to properties.
   * 
   * @param key
   * @param multi
   */
  private static void setFolder(String key, boolean multi)
  {
    // Open file chooser.
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select " + key + " folder");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(multi);
    int option = chooser.showOpenDialog(null);

    // Save folder(s) in properties.
    if (option == JFileChooser.APPROVE_OPTION)
    {
      String value = null;
      if (multi)
      {
        // Convert multiple folder selection to comma separated values.
        for (File selectedFolder : chooser.getSelectedFiles())
        {
          String selectedAbsolutePath = selectedFolder.getAbsolutePath();
          value =
              value == null ? selectedAbsolutePath : value + DELIMITER + selectedAbsolutePath;
        }
      }
      else
      {
        // Get the path of only 1 selected folder.
        value = chooser.getSelectedFile().getAbsolutePath();
      }
      PROP.setProperty(key, value);
    }
  }

  /**
   * Writes to log.
   * 
   * @param msg
   */
  private static void log(String msg)
  {
    System.out.println(new Date() + "\t" + msg);
  }
}
