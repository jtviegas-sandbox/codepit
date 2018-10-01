package org.challenges.rab.statproc.validator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.challenges.rab.statproc.statement.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One simple implementation for the StatementValidator interface
 * 
 * @author jtviegas
 *
 */
class StatementValidatorImpl implements StatementValidator {

	private final Set<Integer> statements;
	private static final Logger logger = LoggerFactory.getLogger(StatementValidatorImpl.class);

	public StatementValidatorImpl() {
		logger.trace("[()|in]");
		statements = new HashSet<Integer>();
		logger.trace("[()|out]");
	}

	@Override
	public boolean validate(Statement s) {
		logger.trace("[validate|in] statement:{}", s);
		boolean r = false;
		if (!statements.contains(s.getReference())) {
			statements.add(s.getReference());
			BigDecimal finalBalance = s.getStartBalance().add(s.getMutation());

			//finalBalance.compareTo(balance);
			//double endBalance = Math.round((s.getStartBalance() + s.getMutation()) * 100.00) / 100.00;
			//r &= (0 == Double.compare(endBalance, s.getEndBalance()));
			r &= (0 == finalBalance.compareTo(s.getEndBalance()));

		}

		logger.trace("[validate|out] => {}", r);
		return r;
	}

	@Override
	public void reset() {
		logger.trace("[reset|in]");
		statements.clear();
		logger.trace("[reset|out]");
	}

}
