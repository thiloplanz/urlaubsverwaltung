package org.synyx.urlaubsverwaltung.application.web;

/**
 * Represents the person that should decide about an application for leave.
 */
public class ReferredPerson {

    private String loginName;

    public String getLoginName() {

        return loginName;
    }


    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }
}
