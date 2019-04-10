package bacnet.e4.rap;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import bacnet.raprcp.NavigationManagement;
import bacnet.swt.ResourceManager;
import bacnet.swt.SWTResourceManager;
import bacnet.views.HelpPage;

public class SubmitDataView implements SelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2315247992526407384L;

	public static final String ID = "bacnet.e4.rap.SubmitDataView"; //$NON-NLS-1$

	/**
	 * Indicates if we focus the view, so we can pushState navigation
	 */
	private boolean focused = false;

	private Composite composite;
	private Text textExpName;
	private Text textSubmitterName;
	private Text textSubmiInstitution;
	private Text txtDescriptionExp;
	private Text textNbDataset;
	private Button btnSubmit;
	private Text textTypeData;
	private Text txtpublication;
	private Text txtDataID;
	private Text txtEmail;
	private Text textSubmit;
	private Button btnPreview;
	private Button btnHelp;

	@Inject
	EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	public SubmitDataView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		focused = true;
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		{
			Label lblXxSrnas = new Label(container, SWT.BORDER | SWT.CENTER);
			lblXxSrnas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblXxSrnas.setFont(SWTResourceManager.getBodyFont(SWT.BOLD));
			lblXxSrnas.setText("Submit your own datasets to Listeriomics website");
			lblXxSrnas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		}
		{
			btnHelp = new Button(container, SWT.NONE);
			btnHelp.setToolTipText("How to submit a dataset ?");
			btnHelp.setImage(ResourceManager.getPluginImage("bacnet", "icons/help.png"));
			btnHelp.addSelectionListener(this);
		}
		{
			ScrolledComposite scrolledComposite = new ScrolledComposite(container,
					SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);
			{
				composite = new Composite(scrolledComposite, SWT.NONE);
				composite.setLayout(new GridLayout(2, false));
				{
					Label lblExperiment = new Label(composite, SWT.NONE);
					lblExperiment.setText("Experiment");
				}
				new Label(composite, SWT.NONE);
				{
					Label lblNameOfYour = new Label(composite, SWT.NONE);
					lblNameOfYour.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblNameOfYour.setText("Name :");
				}
				{
					textExpName = new Text(composite, SWT.BORDER);
					textExpName.setText("name");
					textExpName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
				{
					Label lblExperimentDescription = new Label(composite, SWT.WRAP);
					lblExperimentDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblExperimentDescription.setText("Description");
				}
				{
					txtDescriptionExp = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
					txtDescriptionExp.setText("description");
					GridData gd_txtDescriptionExp = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd_txtDescriptionExp.heightHint = 102;
					txtDescriptionExp.setLayoutData(gd_txtDescriptionExp);
				}
				{
					Label lblPublication = new Label(composite, SWT.NONE);
					lblPublication.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblPublication.setText("Publication");
				}
				{
					txtpublication = new Text(composite, SWT.BORDER);
					txtpublication.setText("publication");
					txtpublication.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				}
				new Label(composite, SWT.NONE);
				new Label(composite, SWT.NONE);
				{
					Label lblTypeOfOmics = new Label(composite, SWT.WRAP | SWT.RIGHT);
					GridData gd_lblTypeOfOmics = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
					gd_lblTypeOfOmics.heightHint = 72;
					gd_lblTypeOfOmics.widthHint = 216;
					lblTypeOfOmics.setLayoutData(gd_lblTypeOfOmics);
					lblTypeOfOmics.setText("Type of omics dataset: Genomics, Transcriptomics, proteomics ?");
				}
				{
					textTypeData = new Text(composite, SWT.BORDER);
					textTypeData.setText("genomics");
					GridData gd_textTypeData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_textTypeData.widthHint = 300;
					textTypeData.setLayoutData(gd_textTypeData);
				}
				{
					Label lblNumberOfDataset = new Label(composite, SWT.NONE);
					lblNumberOfDataset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblNumberOfDataset.setText("Number of datasets");
				}
				{
					textNbDataset = new Text(composite, SWT.BORDER);
					textNbDataset.setText("dataset");
					GridData gd_textNbDataset = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_textNbDataset.widthHint = 109;
					textNbDataset.setLayoutData(gd_textNbDataset);
				}
				{
					Label lblDatabaseIds = new Label(composite, SWT.NONE);
					lblDatabaseIds.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblDatabaseIds.setText("Accession IDs");
				}
				{
					txtDataID = new Text(composite, SWT.BORDER);
					txtDataID.setText("accession");
					GridData gd_txtDataID = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_txtDataID.widthHint = 300;
					txtDataID.setLayoutData(gd_txtDataID);
				}
				{
					Label label = new Label(composite, SWT.NONE);
				}
				new Label(composite, SWT.NONE);
				{
					Label lblOwner = new Label(composite, SWT.NONE);
					lblOwner.setText("Submitter");
				}
				new Label(composite, SWT.NONE);
				{
					Label lblName = new Label(composite, SWT.NONE);
					lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblName.setText("Name");
				}
				{
					textSubmitterName = new Text(composite, SWT.BORDER);
					textSubmitterName.setText("Submitter name");
					GridData gd_textSubmitterName = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_textSubmitterName.widthHint = 300;
					textSubmitterName.setLayoutData(gd_textSubmitterName);
				}
				{
					Label lblEmail = new Label(composite, SWT.NONE);
					lblEmail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblEmail.setText("Email");
				}
				{
					txtEmail = new Text(composite, SWT.BORDER);
					txtEmail.setText("becavin.christophe@gmail.com");
					GridData gd_txtEmail = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_txtEmail.widthHint = 300;
					txtEmail.setLayoutData(gd_txtEmail);
				}
				{
					Label lblInstitution = new Label(composite, SWT.NONE);
					lblInstitution.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					lblInstitution.setText("Institution: ");
				}
				{
					textSubmiInstitution = new Text(composite, SWT.BORDER);
					textSubmiInstitution.setText("institution");
					GridData gd_textSubmiInstitution = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
					gd_textSubmiInstitution.widthHint = 300;
					textSubmiInstitution.setLayoutData(gd_textSubmiInstitution);
				}
				new Label(composite, SWT.NONE);
				new Label(composite, SWT.NONE);
				{
					btnPreview = new Button(composite, SWT.NONE);
					btnPreview.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
					btnPreview.setText("Preview Submission");
					btnPreview.addSelectionListener(this);
				}
				{
					textSubmit = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
					GridData gd_textSubmit = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd_textSubmit.heightHint = 131;
					textSubmit.setLayoutData(gd_textSubmit);
				}
				{
					Composite composite_1 = new Composite(composite, SWT.NONE);
					composite_1.setLayout(new GridLayout(1, false));
					composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));

					btnSubmit = new Button(composite_1, SWT.NONE);
					btnSubmit.setText("Submit to Listeriomics");
					btnSubmit.addSelectionListener(this);
				}
			}
			scrolledComposite.setContent(composite);
			scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		createActions();
	}

	@Focus
	public void onFocus() {
		if (!focused) {
			pushState();
			focused = true;
		} else {
			focused = false;
		}
	}

	/**
	 * Push genome, chromosome, gene and Tabitem state
	 */
	public void pushState() {
		NavigationManagement.pushStateView(SubmitDataView.ID);
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	private String textSubmission() {
		String summary = "Submitted experiment\n";
		summary += "Experiment Name: " + textExpName.getText() + "\n";
		summary += "description: " + txtDescriptionExp.getText() + "\n";
		summary += "Data type: " + textTypeData.getText() + "\n";
		summary += "Publications: " + txtpublication.getText() + "\n";
		summary += "Data number: " + textNbDataset.getText() + "\n";
		summary += "Data accession: " + txtDataID.getText() + "\n";
		summary += "Submitter\n";
		summary += "Name: " + textSubmitterName.getText() + "\n";
		summary += "Email: " + txtEmail.getText() + "\n";
		summary += "Institution: " + textSubmiInstitution.getText() + "\n";
		return summary;
	}

	public static void sendEmail(String[] to, String subject, String body) {
		String server = "smtp.pasteur.fr";
		String username = "cbecavin";
		String password = "2010List";
		String from = "listeriomics@pasteur.fr";

		try {
			Properties properties = new Properties();
			properties.put("mail.smtp.host", server);
			Session emailSession = Session.getDefaultInstance(properties);

			Message emailMessage = new MimeMessage(emailSession);
			emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to[0]));
			for (int i = 1; i < to.length; i++)
				emailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(to[i]));
			emailMessage.setFrom(new InternetAddress(from));
			emailMessage.setSubject(subject);
			emailMessage.setText(body);
			// emailSession.setDebug(true);

			Transport.send(emailMessage);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnSubmit) {
			String summary = textSubmission();
			String email = txtEmail.getText();
			String emailListeriomics = "listeriomics@pasteur.fr";
			System.out.println(summary);
			/*
			 * Send email to Listeriomics webmaster
			 */
			String[] to = { email };
			String subject = "[Listeriomics] New Submission to Listeriomics website";
			String body = summary;
			sendEmail(to, subject, body);
			/*
			 * Send email to submitter
			 */
			System.out.println(email);
			to[0] = email;
			subject = "[Listeriomics] Submission to Listeriomics website";
			body = "Dear " + textSubmitterName.getText()
					+ ",\nThank you for your submission on Listeriomics website.\nWe will get back to you soon for finalizing the process.\nWith Best Regards\nThe Listeriomics team\n\n"
					+ summary;

			if (MessageDialog.openConfirm(shell, "Confirm your submission", summary)) {
				sendEmail(to, subject, body);
				partService.showPart(InitViewListeria.ID, PartState.ACTIVATE);
				MessageDialog.openInformation(shell, "Thank you for submitting to Listeriomics",
						"A submission summary has been sent to " + email);
			}

		} else if (e.getSource() == btnPreview) {
			String summary = textSubmission();
			textSubmit.setText(summary);
		} else if (e.getSource() == btnHelp) {
			HelpPage.helpSubmit(partService);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}
}
