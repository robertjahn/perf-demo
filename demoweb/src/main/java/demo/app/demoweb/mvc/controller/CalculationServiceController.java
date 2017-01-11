package demo.app.demoweb.mvc.controller;

import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import demo.app.demoapp.data.domain.InterestResult;
import demo.app.demoapp.services.CalculationService;
import demo.app.demoapp.services.ServiceException;
import demo.app.demoweb.mvc.data.CompoundInterestRequest;
import demo.app.demoweb.mvc.data.CompoundInterestResponse;
import demo.app.demoweb.mvc.data.Status;

/**
 * Handles requests for the application home page.
 */
@RestController
@RequestMapping("calculate")
public class CalculationServiceController {

	@Autowired
	private CalculationService calculationService;

	@Autowired
	private String calculateActiveVersion;

	protected final static Logger log = LoggerFactory.getLogger(CalculationServiceController.class);

	protected final static String VERSION_1 = "v1";
	protected final static String VERSION_2 = "v2";
	
	
	@RequestMapping(value = "/computeCompoundInterest", method = RequestMethod.POST, produces = "application/json")
	public CompoundInterestResponse computeCompoundInterest(@RequestBody CompoundInterestRequest request) {

		return runComputeCompoundInterest(request, calculateActiveVersion);
		
	}
	
	
	@RequestMapping(value = "/v1/computeCompoundInterest", method = RequestMethod.POST, produces = "application/json")
	public CompoundInterestResponse computeCompoundInterestSync(@RequestBody CompoundInterestRequest request) {

		return runComputeCompoundInterest(request, VERSION_1);

	}


	@RequestMapping(value = "/v2/computeCompoundInterest", method = RequestMethod.POST, produces = "application/json")
	public CompoundInterestResponse computeCompoundInterestAsync(@RequestBody CompoundInterestRequest request) {

		return runComputeCompoundInterest(request, VERSION_2);
	
	}

	private CompoundInterestResponse runComputeCompoundInterest(CompoundInterestRequest request, String version) {

		Long startTime = System.currentTimeMillis();
		log.debug("Calling JSON service computeCompoundInterest (" + version + ")");

		CompoundInterestResponse response = new CompoundInterestResponse();
		response.setVersion(version);
		
		if (request != null) {
			if (request.getAccounts() != null && 
					request.getStartDate() != null && 
					request.getIntervals() != null && 
					request.getFrequency() != null && 
					request.getIncludeBreakdowns() != null) {
				log.debug("Num Accounts: " + request.getAccounts().size());
				log.debug("StartDate: " + request.getStartDate());
				log.debug("Intervals: " + request.getIntervals());
				log.debug("Frequency: " + request.getFrequency());
				log.debug("IncludeBreakdowns: " + request.getIncludeBreakdowns());
				try {
					List<InterestResult> resultList = null;
					
					if (version.equalsIgnoreCase(VERSION_1)) {
						resultList = calculationService.calculateCompoundInterest(request.getAccounts(), request.getStartDate(), 
								request.getIntervals(), request.getFrequency() , request.getIncludeBreakdowns());
					} else if (version.equalsIgnoreCase(VERSION_2)) {
						resultList = calculationService.calculateCompoundInterestAsync(request.getAccounts(), request.getStartDate(), 
								request.getIntervals(), request.getFrequency() , request.getIncludeBreakdowns());
					}

					if (resultList != null) {
						response.setStatus(Status.OK);
						response.setResults(resultList);
						response.setElapsedTimeMs(System.currentTimeMillis() - startTime);
					} else {
						response.setStatus(Status.ERROR);
						response.setMessage("No results were returned.");
						response.setElapsedTimeMs(System.currentTimeMillis() - startTime);
					}
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					response.setStatus(Status.ERROR);
					response.setMessage("An exception occurred: " + e.getLocalizedMessage());
					response.setElapsedTimeMs(System.currentTimeMillis() - startTime);
					e.printStackTrace();
				}
			} else {
				response.setStatus(Status.ERROR);
				response.setMessage("The input account list is null.");
				response.setElapsedTimeMs(System.currentTimeMillis() - startTime);
				log.debug("Accounts list is null.");
			}
		}

		return response;
	}
	
	
	
}