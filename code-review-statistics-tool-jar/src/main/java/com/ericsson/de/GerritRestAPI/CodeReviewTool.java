package com.ericsson.de.GerritRestAPI;

import java.io.Console;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ericsson.de.ChartGen.chartGenerator;
import com.ericsson.de.Models.TotalLinesCommitted;
import com.ericsson.de.Models.Options;
import com.ericsson.de.TablePrinter.TablePrinter;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ApprovalInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;

public final class CodeReviewTool 
{
	private static GerritRestApiFactory gerritRestApi = new GerritRestApiFactory();
	private static GerritAuthData.Basic authData = null;
	private static GerritApi gerritApi = null;
	private static Scanner scanner = new Scanner(System.in);
	private static TablePrinter table = new TablePrinter();
	

	public static void main(String[] args) throws RestApiException 
	{	
		login();
		mainMenu();
		
		scanner.close();
	}
	
	private static void login ()
	{		
		/*
		System.out.println("Signum: ");
		String signum = scanner.nextLine();
		System.out.println("Password: ");
		String password = scanner.nextLine();
		 */
		Console console = System.console();
		String signum = null;
		
        if (console == null)
        {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        
        signum = console.readLine("Signum: ");
        char password[] = console.readPassword("Password: ");
	
		authData = new GerritAuthData.Basic("https://gerrit.ericsson.se", signum, new String(password));
		gerritApi = gerritRestApi.create(authData);
			
		try 
		{
			List<ChangeInfo> loginTest = gerritApi.changes().query("status:merged+project:OSS/com.ericsson.ci.test/onboardingeight&o=LABELS&o=DETAILED_LABELS&o=DETAILED_ACCOUNTS").withLimit(5).get();
		} 
		catch (RestApiException e) 
		{
			System.out.println("Invalid Login!! Try again!\n");
			login();
			//e.printStackTrace();
		}
		
	}
	
	private static void mainMenu () throws RestApiException
	{
		int optValue = 0;
		
		Options option1 = new Options("e", "email", "Email", "1", 1);
		Options option2 = new Options("g", "group", "Group", "2", 2);
		Options option3 = new Options("p", "project", "Project", "3", 3);
		Options option4 = new Options("q", "quit", "Quit", "4", 4);
		
		ArrayList<Options> options = new ArrayList<Options>();
		options.add(option1);
		options.add(option2);
		options.add(option3);
		options.add(option4);
		
		TablePrinter mainMenu = new TablePrinter("","What Information would you like see ");
		mainMenu.addRow("1. ", " Email");
		mainMenu.addRow("2. ", " Guardian Group");
		mainMenu.addRow("3. ", " Project");
		mainMenu.addRow("4. ", " Quit");
		mainMenu.print();
		
		boolean again= true;
		
		while (again)
		{
			again= false;
			
			String option = scanner.nextLine();
			
			for (Options opt : options)
			{
				if (option.equals(opt.getChrctr()) || option.equals(opt.getFull()) || option.equals(opt.getCapitalFull()) || option.equals(opt.getNumber()))
				{
					optValue = opt.getValue();
					break;
				}
			}
			
			switch(optValue)
			{
			case 1:
				limitByPerson();
				break;
			case 2:
				limitByGroup();
				break;
			case 3:
				limitByProject();
				break;
			case 4:
				return;
			default:
				System.out.println("Invalid Option! Please try again.");
				again=true;
				break;
			}
		}
		
		mainMenu();
	}
	
	
	private static void limitByPerson () throws RestApiException
	{		
		TablePrinter menu2 = new TablePrinter(""," Enter the e-mail of the person you would like information on:");
		menu2.addRow("e.g "," john.doe@ericsson.com");
		menu2.addRow("", "");
		menu2.addRow("<-", "Go Back to Main Menu");
		menu2.print();
		
		String input = scanner.nextLine();
		
		if (input.equals("back") || input.equals("b"))
		{
			return;
		}
		
		String emailQuery = "owner:" + input;
		
		int limit = howManyCommits();
		int viewChoice = displayMenu();

		switch(viewChoice)
		{
		case 1:
			createPecentage(createQuery(emailQuery,limit));
			break;
		case 2:
			boolean anonymousAuthor = anonymousView();
			creatTableInfo(createQuery(emailQuery,limit), anonymousAuthor);
			table.print();
			break;
		case 3:			
			table = new TablePrinter("Author", "Total Line's of Code Added");
			showLinesOfCode(createQuery(emailQuery,limit));
			table.print();
			break;
		default:
			System.out.println("Invalid entry, please try again.");
			break;
		}
	}
	
	private static void limitByGroup () throws RestApiException
	{
		List<ChangeInfo> masterList = new ArrayList<ChangeInfo>();
		
		TablePrinter menu3 = new TablePrinter(""," Enter the UUID of the guard group you would like information on:");
		menu3.addRow(""," Gerrit ->people ->guard group ->general. e.g 62bf9fa4746c05c0d9gd275373db5e9028272955");
		menu3.print();
		
		String group = scanner.nextLine();
		
		List<String> emails = guardGroupEmails(group);
		
		int limit = howManyCommits();
		
		for (String groupEmail : emails)
		{
			if(groupEmail != null)
			{
				String groupQuery = "owner:" + groupEmail;
				List<ChangeInfo> changes = gerritApi.changes().query("status:merged+" + groupQuery + "&o=LABELS&o=DETAILED_LABELS&o=DETAILED_ACCOUNTS").withLimit(limit).get();
				masterList.addAll(changes);
			}
		}
		Collections.sort(masterList, new Comparator<ChangeInfo>()
		{

			public int compare(ChangeInfo arg0, ChangeInfo arg1) {
				return arg1.updated.compareTo(arg0.updated);
			}
			
		});
		
		masterList = masterList.subList(0, limit);	
		int viewChoice = displayMenu();
			
		switch (viewChoice)
		{
		case 1:
			createPecentage(masterList);
			break;
			
		case 2:
			boolean anonymousAuthor = anonymousView();
			creatTableInfo(masterList, anonymousAuthor);
			table.print();
			break;
			
		case 3:
			table = new TablePrinter("Author", "Total Line's of Code Added");
			showLinesOfCode(masterList);
			table.print();
			break;
			
		default:
			System.out.println("Invalid entry, please try again.");
			break;
		}
	}
	
	private static void limitByProject () throws RestApiException
	{
		TablePrinter menu3 = new TablePrinter(""," Enter the full name of the project you would like information on:");
		menu3.addRow("e.g "," OSS/com.ericsson.... etc");
		menu3.print();
		
		String projectQuery = "project:" + scanner.nextLine();
		
		int limit = howManyCommits();
		
		int viewChoice = displayMenu();
		
		switch(viewChoice)
		{
		case 1:
			createPecentage(createQuery(projectQuery,limit));
			break;
				
		case 2:	
			boolean anonymousAuthor = anonymousView();
			creatTableInfo(createQuery(projectQuery,limit), anonymousAuthor);
			table.print();
			break;
				
		case 3:
			table = new TablePrinter("Author", "Total Line's of Code Added");
			showLinesOfCode(createQuery(projectQuery,limit));
			table.print();
			break;
					
		default:
			System.out.println("Invalid entry, please try again.");
			break;
		}	
	}
	
	private static int howManyCommits ()
	{
		boolean again = true;
		int choice = 1;
		TablePrinter limitMenu = new TablePrinter(""," Enter the number of commits to limit the query :");
		limitMenu.addRow("e.g "," 20");
		limitMenu.print();
		
		while(again)
		{
			choice = 1;
			if(scanner.hasNextInt())
			{
				choice = scanner.nextInt();
					again = false;
			}
			else
			{
				scanner.nextLine();
				System.out.println("Invalid input try again");
				
			}
		}
		return choice;
	}
	
	private static int displayMenu ()
	{
		boolean again = true;
		String option = null;
		int optValue = 0;
		
		Options option1 = new Options("p", "percentage", "Perentage", "1", 1);
		Options option2 = new Options("table", "Table", "detailed table", "2", 2);
		Options option3 = new Options("lines", "lines of code", "Lines", "3", 3);
		
		ArrayList<Options> options = new ArrayList<Options>();
		options.add(option1);
		options.add(option2);
		options.add(option3);
		
		TablePrinter resultsMenu = new TablePrinter(""," Would you like to see results as:");
		resultsMenu.addRow("1. "," Percentage following guildlines?");
		resultsMenu.addRow("2. "," In a detailed table?: ");
		resultsMenu.addRow("3. "," Line's of Code Submitted?: ");
		resultsMenu.print();


		while(again)
		{
			again = false;
			scanner.nextLine();
			option = scanner.nextLine();
			
			for (Options opt : options)
			{				
				if (option.equals(opt.getChrctr()) || option.equals(opt.getFull()) || option.equals(opt.getCapitalFull()) || option.equals(opt.getNumber()))
				{
					optValue = opt.getValue();
					break;
				}
			}

			switch (optValue)
			{
			case 1:
			case 2:
			case 3:
				return optValue;
				
			default:
				System.out.println("Invalid entry, please try again.");
				again = true;
				break;
			}
		}
		return optValue;
	}
	
	public static boolean anonymousView ()
	{
		int optValue = 0;
		
		Options option1 = new Options("y", "yes", "Yes", "1", 1);
		Options option2 = new Options("n", "no", "No", "2", 2);
		
		ArrayList<Options> options = new ArrayList<Options>();
		options.add(option1);
		options.add(option2);
		
		TablePrinter anonymousMenu = new TablePrinter("", "Would you like the information displayed anonymously?: ");
		anonymousMenu.addRow("1. "," Yes: ");
		anonymousMenu.addRow("2. "," No: ");
		anonymousMenu.print();
		
		String answer = scanner.nextLine();
		
		for (Options opt : options)
		{				
			if (answer.equals(opt.getChrctr()) || answer.equals(opt.getFull()) || answer.equals(opt.getCapitalFull()) || answer.equals(opt.getNumber()))
			{
				optValue = opt.getValue();
				break;
			}
		}
		
		switch(optValue)
		{
		case 2:
			table = new TablePrinter("Author","Subject","Date","+2 their own code","Straight to master");
			return false;
		case 1 :
			table = new TablePrinter("Subject","Date","+2 their own code","Straight to master");
			return true;
		default:
			anonymousView ();
		}
		return false;
	}
	
	private static List<String> guardGroupEmails(String uuid) throws RestApiException
	{
		List<AccountInfo> groups =  gerritApi.groups().id(uuid).members();
		List<String> listOfEmails = new ArrayList<String>();
		
		for(AccountInfo accountInfo : groups)
		{
			listOfEmails.add(accountInfo.email);
		}
		
		return listOfEmails;
	}
	
	private static List<ChangeInfo> createQuery(String queryParam, int limit)throws RestApiException 
	{
		List<ChangeInfo> changes = gerritApi.changes()
				.query("status:merged+" + queryParam + "&o=LABELS&o=DETAILED_LABELS&o=DETAILED_ACCOUNTS")
				.withLimit(limit).get();
		return changes;
	}
	
	private static void creatTableInfo (List<ChangeInfo> changes, boolean anonymousMode) throws RestApiException
	{		
		for (ChangeInfo cc : changes)
		{
			Date date = new Date(cc.updated.getTime());
			if (date.after(dateLimit())) 
			{
				int count = 0 ;
				String selfReview = "No";
				String strightToMaster = "No";
				if (cc.labels.get("Code-Review") == null)
				{
					strightToMaster = "Yes";
				}
				else
				{
					for (ApprovalInfo ap : cc.labels.get("Code-Review").all)
					{
					  	if (ap.value == 1)
					   	{
					   		count ++;
					   	}
					   	if (cc.owner.name.equals(ap.name) && ap.value == 2 && count == 0) {
					   		selfReview = "Yes";
						} 
					}
				}
				
				if (anonymousMode)
				{
					try{
						table.addRow(cc.subject.substring(0, 12),cc.updated.toString(),selfReview,strightToMaster);
					}catch(Exception e)
					{
						table.addRow(cc.subject,cc.updated.toString(),selfReview,strightToMaster);
					}
					
				}
				else
				{
					try{
						table.addRow(cc.owner.name,cc.subject.substring(0, 12),cc.updated.toString(),selfReview,strightToMaster);
					}catch(Exception e)
					{
						table.addRow(cc.owner.name,cc.subject,cc.updated.toString(),selfReview,strightToMaster);
					}
					
				}
			}
		}
	}
	

	
	private static void createPecentage(List<ChangeInfo> changes) throws RestApiException 
	{
		int total = 0;
		int correct = 0;
		
		for (ChangeInfo cc : changes) 
		{
			int plus1 = 0;
			int plus2 = 0;
			total++;
			if (cc.labels.get("Code-Review") != null) 
			{
				for (ApprovalInfo ap : cc.labels.get("Code-Review").all) 
				{
					if (ap.value == 1) 
						plus1++;
					 
					else if (ap.value == 2 && !cc.owner.name.equals(ap.name)) 
						plus2++;
				}
			}
			if (plus1 > 1) 
			{
				correct++;
			}
			if (plus1 > 0 && plus2 > 0) 
			{
				correct++;
			}
		}
		double result = correct * 100 / total;
		System.out.println("% of code following codeing guildlines = " + result + "%");
		chartGenerator chart = new chartGenerator(result,(100-result));
		chart.pack();
		chart.toFront();
		chart.setVisible(true);
		
	}

	private static void showLinesOfCode (List<ChangeInfo> changes) throws RestApiException
	{
		List<String> authors = new ArrayList<String>();
		int totalLinesAdded = 0;
		TotalLinesCommitted authorsLines = null;
		Map<String, Integer> resultMap = new HashMap<String,Integer>();
		List<TotalLinesCommitted> allAuthors = new ArrayList<TotalLinesCommitted>();
		
		for (ChangeInfo cc : changes)
		{
			if (!authors.contains(cc.owner.name))
			{
				totalLinesAdded = 0;
				authors.add(cc.owner.name);
					
				for (ChangeInfo ccLines : changes)
				{
					if (ccLines.owner.name.equals(cc.owner.name))
						totalLinesAdded += ccLines.insertions;
				}
				
				authorsLines = new TotalLinesCommitted(cc.owner.name, totalLinesAdded);
				
				if (!allAuthors.contains(authorsLines))
					allAuthors.add(authorsLines);
			}
		}
		
		Collections.sort(allAuthors, new Comparator<TotalLinesCommitted>()
		{
			public int compare(TotalLinesCommitted o1, TotalLinesCommitted o2) 
			{
				if (o1.getLines() > o2.getLines()) return -1;
				else if (o1.getLines() < o2.getLines()) return 1;
				else return 0;
			}
			
		});
		
		for (TotalLinesCommitted aa : allAuthors)
		{
			table.addRow(aa.getName(), Integer.toString(aa.getLines()));
			resultMap.put(aa.getName() + " " + aa.getLines(), aa.getLines());
		}
		chartGenerator chart = new chartGenerator(resultMap);
		chart.pack();
		chart.toFront();
		chart.setVisible(true);
		
	}
	
	private static Date dateLimit()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -120);
		String dateInString = sdf.format(cal.getTime());
		Date olddate = null;
		try 
		{
			olddate = sdf.parse(dateInString);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		return olddate;
	}
	
}