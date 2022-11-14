package com.igormaznitsa.mindmap.annoit.case1.sub3.beta;

import com.igormaznitsa.mindmap.annotations.MmdFileLink;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileLink(uid = "beta")
public class Beta1 {
  @MmdTopic
  public void beta() {

  }

  @MmdTopic(fileUid = "gamma", path = {"p1", "p2", "beta_"})
  public void path2() {

  }

}
