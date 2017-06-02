package com.clover.remote.client.messages;

/**
 * Created by Avdhesh Akhani on 12/22/16.
 */

public class CardApplicationIdentifier {

    private String applicationLabel;
    private String applicationIdentifier;
    private String priorityIndex;
    private boolean performAidSelection;

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    public String getApplicationIdentifier() {
        return applicationIdentifier;
    }

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;
    }

    public String getPriorityIndex() {
        return priorityIndex;
    }

    public void setPriorityIndex(String priorityIndex) {
        this.priorityIndex = priorityIndex;
    }

    public boolean isPerformAidSelection() {
        return performAidSelection;
    }

    public void setPerformAidSelection(boolean performAidSelection) {
        this.performAidSelection = performAidSelection;
    }
}
