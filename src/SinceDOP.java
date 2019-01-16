import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

/**
 * SinceDOP is created for AppleCare Advisors to determine eligibility to purchase AppleCare+.
 * The original date of purchase is selected from the JDatePicker and the user will also have
 * the option to select the date to check. The date to check is automatically set to the current
 * date by default. Once dates are selected and you click "Check", the number of days since the
 * DOP is displayed. 
 * 
 * Green = < 60, eligible
 * Red = > 60, not eligible
 * Yellow = 60, eligible until 12AM
 * 
 * @author LoganDuck
 * @see https://github.com/JDatePicker
 * @version 2.0.1
 */
public class SinceDOP {
	private static JFrame frame;
	private static UtilDateModel dopDateModel, checkDateModel;
	private static JDatePickerImpl startDatePicker, endDatePicker;
	private static JButton check, reset;
	private static JPanel daysSincePanel;
	private static JLabel daysSinceDOP;
	private final Dimension frameSize = new Dimension(222, 300);
	
	private static int distance = 0;
	
	/* constructor */
	public SinceDOP() {
		frame = new JFrame();
		frame.setTitle("SinceDOP");
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setMinimumSize(frameSize);
		frame.setMinimumSize(frameSize);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.setLocationRelativeTo(null);

		/*
		 * Sets the model of the date to check to default to the current date. The user is given the option to select
		 * a different date to check against the DOP, but most of the time users will need to check the current date.
		 */
		checkDateModel = new UtilDateModel();
		checkDateModel.setSelected(true);
		checkDateModel.setDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		
		dopDateModel = new UtilDateModel();
		/*
		 * Initially the value for dopDateModel is null, because no date is set "Check" needs to be disabled.
		 * When a new date is selected for dopDateModel, the state therefore changes and "Check" is enabled. 
		 */
		dopDateModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				check.setEnabled(true);
			}
		});
		
		initLabel("Original DOP", 10);
		initLabel("Date to Check", 60);

		startDatePicker = initPicker(dopDateModel, 25);
		endDatePicker = initPicker(checkDateModel, 75);
	
		check = new JButton("Check");
		check.setFont(new Font("Default", Font.BOLD, 16));
		check.setBounds(64, 115, 94, 30);
		check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date originalDOPSelection = (Date) startDatePicker.getModel().getValue();
				Date dateToCheckSelection = (Date) endDatePicker.getModel().getValue();

				String startMonth = convertMonth(originalDOPSelection.toString().substring(4, 7));
				String startDay = originalDOPSelection.toString().substring(8, 10);
				String startYear = originalDOPSelection.toString().substring(24);
				
				String endMonth = convertMonth(dateToCheckSelection.toString().substring(4, 7));
				String endDay = dateToCheckSelection.toString().substring(8, 10);
				String endYear = dateToCheckSelection.toString().substring(24);
				
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				Date startDate = null;
				Date endDate = null;
				try {
					startDate = formatter.parse(startMonth + "/" + startDay + "/" + startYear);
					endDate = formatter.parse(endMonth + "/" + endDay + "/" + endYear);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				
				Calendar start = Calendar.getInstance();
				start.setTime(startDate);
				Calendar end = Calendar.getInstance();
				end.setTime(endDate);

				distance = 0;
				for (; start.before(end); start.add(Calendar.DATE, 1), start.getTime()) {
					distance++;
				}
				
				repaintLabel();
			}
		});
		check.setEnabled(false);
		frame.add(check);
		
		reset = new JButton("Reset");
		reset.setFont(new Font("Default", Font.BOLD, 16));
		reset.setBounds(64, 245, 94, 30);
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dopDateModel.setValue(null);
				dopDateModel.setDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
				
				startDatePicker = initPicker(dopDateModel, 25);
				
				daysSinceDOP.setText("0");
				daysSinceDOP.setForeground(Color.WHITE);
				check.setEnabled(false);
			}
		});
		frame.add(reset);
		
		daysSincePanel = new JPanel();
		daysSincePanel.setBackground(Color.DARK_GRAY);
		daysSincePanel.setBounds(0, 140, 222, 80);
		frame.add(daysSincePanel);
		
		daysSinceDOP = new JLabel("0");
		daysSinceDOP.setFont(new Font("Default", Font.BOLD, 60));
		daysSinceDOP.setForeground(Color.WHITE);
		daysSinceDOP.setHorizontalAlignment(SwingConstants.CENTER);
		daysSinceDOP.setBounds(0, 0, 80, 72);
		daysSincePanel.add(daysSinceDOP);
		
		frame.setVisible(true);
	}
	
	/**
	 * Sets the date pickers.
	 * @param model the date model for the picker.
	 * @param y coordinate for the bounds.
	 * @return date picker.
	 */
	public static JDatePickerImpl initPicker(UtilDateModel model, int y) {
		JDatePickerImpl picker = new JDatePickerImpl(new JDatePanelImpl(model));
		picker.setBackground(Color.DARK_GRAY);
		picker.setBounds(10, y, 202, 30);
		frame.add(picker);
		return picker;
	}
	
	/**
	 * Sets the "Original DOP" & "Date to Check" labels.
	 * @param title the text for the label.
	 * @param y coordinate for the bounds.
	 */
	public static void initLabel(String title, int y) {
		JLabel label = new JLabel(title);
		label.setFont(new Font("Default", Font.BOLD, 12));
		label.setForeground(Color.WHITE);
		label.setBounds(10, y, label.getPreferredSize().width, 15);
		frame.add(label);
	}
	
	/**
	 * Depending on the distance between the dates, the color of the label is set.
	 */
	public static void repaintLabel() {
		daysSinceDOP.setText(distance + "");
		if (distance == 0) {
			daysSinceDOP.setForeground(Color.WHITE);
		} else if (distance < 60) {
			daysSinceDOP.setForeground(Color.GREEN);
		} else if (distance == 60) {
			daysSinceDOP.setForeground(Color.YELLOW);
		} else {
			daysSinceDOP.setForeground(Color.RED);	
		} 
	}
	
	/**
	 * Converts the abbreviated month into the respective number format.
	 * @param month to convert.
	 * @return converted number of the abbreviated month.
	 */
	public static String convertMonth(String month) {
		switch (month) {
			case "Jan": return "01";
			case "Feb": return "02";
			case "Mar": return "03";
			case "Apr": return "04";
			case "May": return "05";
			case "Jun": return "06";
			case "Jul": return "07";
			case "Aug": return "08";
			case "Sep": return "09";
			case "Oct": return "10";
			case "Nov": return "11";
			case "Dec": return "12";
		}
		return "";
	}
	
	/* main */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
	            new SinceDOP();
            }
        });
	}
}