/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2017-2020 Sigwo Technologies
 * Copyright © 2020-2021 Jupiter Project Developers
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.db.DbIterator;
import nxt.util.Convert;

public final class GetBlockchainTransactions extends APIServlet.APIRequestHandler {

    static final GetBlockchainTransactions instance = new GetBlockchainTransactions();

    private GetBlockchainTransactions() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.TRANSACTIONS}, "account", "timestamp", "type", "subtype",
                "firstIndex", "lastIndex", "numberOfConfirmations", "withMessage", "phasedOnly", "nonPhasedOnly",
                "includeExpiredPrunable", "includePhasingResult", "executedOnly", "message");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long accountId = ParameterParser.getAccountId(req, true);
        int timestamp = ParameterParser.getTimestamp(req);
        int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);
        boolean withMessage = "true".equalsIgnoreCase(req.getParameter("withMessage"));
        boolean phasedOnly = "true".equalsIgnoreCase(req.getParameter("phasedOnly"));
        boolean nonPhasedOnly = "true".equalsIgnoreCase(req.getParameter("nonPhasedOnly"));
        boolean includeExpiredPrunable = "true".equalsIgnoreCase(req.getParameter("includeExpiredPrunable"));
        boolean includePhasingResult = "true".equalsIgnoreCase(req.getParameter("includePhasingResult"));
        boolean executedOnly = "true".equalsIgnoreCase(req.getParameter("executedOnly"));
        String messageToFilter = req.getParameter("message");

        byte type;
        byte subtype;
        try {
            type = Byte.parseByte(req.getParameter("type"));
        } catch (NumberFormatException e) {
            type = -1;
        }
        try {
            subtype = Byte.parseByte(req.getParameter("subtype"));
        } catch (NumberFormatException e) {
            subtype = -1;
        }

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray transactions = new JSONArray();
        try (DbIterator<? extends Transaction> iterator = Nxt.getBlockchain().getTransactions(accountId, numberOfConfirmations,
                type, subtype, timestamp, withMessage, phasedOnly, nonPhasedOnly, firstIndex, lastIndex,
                includeExpiredPrunable, executedOnly)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                
                if (type >= 0 && Byte.compare(type, transaction.getType().getType()) != 0) continue;

                if (withMessage) {
	            	if (transaction.getMessage() == null) continue;
	
	            	if (messageToFilter != null && !messageToFilter.isEmpty()) {
	                	String messageString = Convert.toString(transaction.getMessage().getMessage(), transaction.getMessage().isText());
	                	if (!messageString.contains(messageToFilter)) {
	                		continue;
	                	}
	                }
                }
               
                transactions.add(JSONData.transaction(transaction, includePhasingResult));
            }
        }

        JSONObject response = new JSONObject();
        response.put("transactions", transactions);
        return response;

    }

}
