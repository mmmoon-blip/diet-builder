package com.dietbutler.pecs.solution;

import java.util.List;

public class Solution {
    private String title;
    private String summary;
    private List<String> advices;
    private String reasoning;

    public Solution() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getAdvices() { return advices; }
    public void setAdvices(List<String> advices) { this.advices = advices; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
