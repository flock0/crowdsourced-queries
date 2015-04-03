package crowdsourced.http;

/** This interface describes the requests done by the webserver to the rest of
 *  the application.
 */
public interface QueryInterface {
    /** Get the infos of all the queries.
     * This return a JSON Object containing all the information (state,
     * progress, ...) about all the queries. See examples/queriesInfo.json for
     * an example JSON Object.
     */
    String queriesInfo();

    /** Create a new query.
     * @param question The question string submitted by the user.
     * @return A JSON object containing the status of the query (succussful or
     * not) and a textual message. See examples/newQuery.json for an example
     * JSON Object.
     */
    String newQuery(String question);

    /** Abort a query.
     * @param question The query ID of the query to abort.
     */
    void abortQuery(String queryId);
}
