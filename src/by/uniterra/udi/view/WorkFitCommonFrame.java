package by.uniterra.udi.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import by.uniterra.dai.eao.ServiceBaseEAO;

public class WorkFitCommonFrame
{

    public static void main(String[] args)
    {
        // Locale.setDefault(new Locale("ru"));
        // CommonDataTablePanel panelYear = new CommonDataTablePanel(new
        // YearTableModel(), new YearOptionPanel(), new
        // YearEAO(ServiceBaseEAO.getDefaultEM()));
        // CommonDataTablePanel panelMonth = new CommonDataTablePanel(new
        // MonthTableModel(), new MonthOptionPanel(), new
        // MonthEAO(ServiceBaseEAO.getDefaultEM()));
        //CommonDataTablePanel panelWorker = new CommonDataTablePanel(new WorkerTableModel(), new WorkerOptionPanel(), new WorkerEAO(
                //ServiceBaseEAO.getDefaultEM()));
        // CommonDataTablePanel panelHoliday = new CommonDataTablePanel(new
        // HolidayTableModel(), new HolidayOptionPanel(), new
        // HolidayEAO(ServiceBaseEAO.getDefaultEM()));

        //JPanel panelCommon = new JPanel();
        //panelCommon.add(panelMonth);
        //panelCommon.add(panelYear);
        //panelCommon.add(panelWorker);
        //panelCommon.add(panelHoliday);
        //CommonDataTablePanel panelDoW = new CommonDataTablePanel(new DaysOfWorkTableModel(), new DaysOfWorkOptionPanel(), new DaysOfWorkEAO(
        //        ServiceBaseEAO.getDefaultEM()));

        //panelCommon.add(panelDoW);
        // show editor
        //JOptionPane.showMessageDialog(null, panelCommon, "Main Frame", JOptionPane.PLAIN_MESSAGE);
        // save to DB
       // panelMonth.writeValues();
        //panelYear.writeValues();
        //panelWorker.writeValues();
       //panelHoliday.writeValues();
        // panelDoW.writeValues();
        //ServiceBaseEAO.disconnectFromDb();

        final JFrame frame = new JFrame(); 
        JButton btnImport = new JButton("Import");
        btnImport.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                createFileChooser(frame);
            }
        });
        frame.add(btnImport);
        //JOptionPane.showMessageDialog(null, btnImport, "Main Frame", JOptionPane.PLAIN_MESSAGE);
        frame.setSize(200, 200);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    

    private static void createFileChooser(final JFrame frame) 
    {
        String filename = File.separator+"tmp";
        JFileChooser fileChooser = new JFileChooser(new File(filename));
        fileChooser.showOpenDialog(frame);
        System.out.println("File to open: " + fileChooser.getSelectedFile());
        fileChooser.getSelectedFile().toPath();
        try
        {
            List<String> lstOriginalData = Files.readAllLines(fileChooser.getSelectedFile().toPath(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            //Log.error(this, e, "createFileChooser error ");
        }
        
        
    }

}
