package agreementadmin;
import java.awt.Color;
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
 * SinceDOP assists Agreement Administration advisors in determining the number of days since a products 
 * original date of purchase, or activation date, to validate purchase of AppleCare+ for the product. 
 * 
 * The original date displays in the model under the "Date of Purchase" label and is selected by the user. Today's
 * date is defaulted, but the user has the option to select an earlier or later date.
 * 
 * Once the dates are selected, the user clicks "Check" and the number of days between the dates is displayed. 
 * Note: AppleCare+ is available for devices up to 60 days.
 * 
 * Green foreground: less than 60 days, eligible to purchase.
 * Yellow foreground: 60th day, must be purchased today.
 * Red foreground: greater than 60 days, not eligible for coverage through Apple. Can be referred to carrier to inquire options.
 * 
 * @author LoganDuck
 * Please send feedback or issues to 'logan_duck@apple.com' with subject SinceDOP.
 * @see https://github.com/JDatePicker
 * @version 3
 */
public class SinceDOP extends JPanel {
	private static final long serialVersionUID = 1L;

	private static JLabel daysSinceDOP;
	
	private static int calculatedDays = 0;
	private static JButton checkButton, resetButton;
	private static final int W = 222, H = 300;
	private Font lblFont = new Font("", Font.BOLD, 12);
	private Font btnFont = new Font("", Font.BOLD, 16);
	private JDatePickerImpl dopPickerImp, todayPickerImp;
	
	public SinceDOP() {
		setLayout(null);
		setBackground(Color.DARK_GRAY);
		setSize(W, H);
		
		JLabel dopLbl = new JLabel("Date of Purchase");
		dopLbl.setFont(lblFont);
		dopLbl.setForeground(Color.WHITE);
		dopLbl.setBounds(10, 10, dopLbl.getPreferredSize().width, 15);
		add(dopLbl);
		
		JLabel todayDateLbl = new JLabel("Today's Date");
		todayDateLbl.setFont(lblFont);
		todayDateLbl.setForeground(Color.WHITE);
		todayDateLbl.setBounds(10, 60, todayDateLbl.getPreferredSize().width, 15);
		add(todayDateLbl);
		
		UtilDateModel dopDateModel = new UtilDateModel();
		dopDateModel.addChangeListener(new ChangeListener() {
			// "Check" button will be enabled when the date of purchase is set.
			@Override
			public void stateChanged(ChangeEvent e) {
				checkButton.setEnabled(true);
			}
		});
		dopPickerImp = new JDatePickerImpl(new JDatePanelImpl(dopDateModel));
		dopPickerImp.setBackground(Color.DARK_GRAY);
		dopPickerImp.setBounds(10, 25, 202, 30);
		add(dopPickerImp);
		
		UtilDateModel todayDateModel = new UtilDateModel();
		todayDateModel.setDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		todayDateModel.setSelected(true); // shows today's date in the model.
		todayPickerImp = new JDatePickerImpl(new JDatePanelImpl(todayDateModel));
		todayPickerImp.setBackground(Color.DARK_GRAY);
		todayPickerImp.setBounds(10, 75, 202, 30);
		add(todayPickerImp);
		
		checkButton = new JButton("Check");
		checkButton.setFont(btnFont);
		checkButton.setBounds(64, 115, 94, 30);
		checkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				Calendar startDate = Calendar.getInstance();
				Calendar endDate = Calendar.getInstance();
				try {
					startDate.setTime(dateEntryFormatter(dopPickerImp));
					endDate.setTime(dateEntryFormatter(todayPickerImp));
					
					calculatedDays = 0;
					for (; startDate.before(endDate); startDate.add(Calendar.DATE, 1), startDate.getTime()) {
						calculatedDays++;
					}
					repaintLabel(); // updates the foreground color and value dependent on the calculated number of days.
				} catch (ParseException error) {
					System.out.println("Parse date error: " + error.getMessage());
				}
			}
		});
		checkButton.setEnabled(false);
		add(checkButton);
		
		JPanel daysSincePanel = new JPanel();
		daysSincePanel.setBackground(Color.DARK_GRAY);
		daysSincePanel.setBounds(0, 140, 222, 80);
		add(daysSincePanel);
		
		daysSinceDOP = new JLabel("0");
		daysSinceDOP.setFont(new Font("", Font.BOLD, 60));
		daysSinceDOP.setForeground(Color.WHITE);
		daysSinceDOP.setHorizontalAlignment(SwingConstants.CENTER);
		daysSinceDOP.setBounds(0, 0, 80, 72);
		daysSincePanel.add(daysSinceDOP);
		
		resetButton = new JButton("Reset");
		resetButton.setFont(btnFont);
		resetButton.setBounds(64, 245, 94, 30);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dopDateModel.setValue(null); // when reset button is clicked, the date of purchase model will not have a date set
				
				dopPickerImp = new JDatePickerImpl(new JDatePanelImpl(dopDateModel));
				
				daysSinceDOP.setText("0");
				daysSinceDOP.setForeground(Color.WHITE);
				checkButton.setEnabled(false);
			}
		});
		add(resetButton);
	}

	/*
	 * Formats the date for the calculation.
	 */
	public Date dateEntryFormatter(JDatePickerImpl picker) throws ParseException {
		Date date = (Date) picker.getModel().getValue();		
		return new SimpleDateFormat("MM/dd/yyyy").parse(convertMonth(date.toString().substring(4, 7)) + "/" + date.toString().substring(8, 10) + "/" + date.toString().substring(24));
	}
	

	/*
	 * Depending on the distance between the dates, the color of the label is set.
	 */
	public static void repaintLabel() {
		daysSinceDOP.setText(String.valueOf(calculatedDays));
		if (calculatedDays == 0) {
			daysSinceDOP.setForeground(Color.WHITE);
		} else if (calculatedDays < 60) {
			daysSinceDOP.setForeground(Color.GREEN);
		} else if (calculatedDays == 60) {
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
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	JFrame frame = new JFrame("SinceDOP");
        		frame.setLayout(null);
        		frame.setLocationRelativeTo(null);
        		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		frame.setSize(W, H);
        		frame.setResizable(false);
        		frame.getContentPane().setBackground(Color.DARK_GRAY);
        		
        		frame.add(new SinceDOP());
        		
        		frame.setVisible(true);
            }
        });
	}
}