package com.appsflyer;

/**
 * Created with IntelliJ IDEA.
 * User: gilmeroz
 * Date: 12/10/13
 * Time: 3:39 PM
 */
class AttributionIDNotReady extends Exception {
    AttributionIDNotReady() {
        super("Data was not received from server yet.");
    }
}
