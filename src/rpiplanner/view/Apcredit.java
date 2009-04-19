package rpiplanner.view;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;

import rpiplanner.RubyEnvironment;

import rpiplanner.model.PlanOfStudy;
import rpiplanner.model.Course;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Feb 24, 2009
 * Time: 6:42:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Apcredit {
    private JPanel tests;
    private JPanel buttons;
    private JComboBox Score;
    private JComboBox Test;
    private JButton AddApButton;
    private JButton CancelButton;
    private JButton OkButton;
    private JPanel panel1;
    private PlanOfStudy plan;
    public int numtests = 1;

    public Apcredit() {
        CancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

            }
        });
        OkButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                save();
            }
        });
        AddApButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if (numtests <= 10)
                {
                    Apcredit newGUI = new Apcredit();
                    tests.add(newGUI.Test, new CellConstraints(1, numtests*2+1));
                    tests.add(newGUI.Score, new CellConstraints(2, numtests*2+1));

                    tests.revalidate();

                    numtests++;
                }

            }
        });
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public JButton getOkButton() {
        return OkButton;
    }
    
    public void setPlanOfStudy(PlanOfStudy plan){
        this.plan = plan;
    }

    public static void main(String[] args) {
        final JFrame window = new JFrame("AP Credit");
        window.setBounds(100, 100, 500, 400);
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(new Apcredit().panel1, BorderLayout.CENTER);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private void save(){
        ArrayList<String> tests = new ArrayList<String>();
        ArrayList<String> scores = new ArrayList<String> ();

        String test = null;
        for(Component c : this.tests.getComponents()){
            JComboBox box = (JComboBox)c;
            if (test == null)
            {
                test = (String)box.getSelectedItem();
            }
            else
            {
                String score = (String)box.getSelectedItem();
                tests.add(test);
                scores.add(score);
                test = null;
            }
        }

        Course[] apcourses = RubyEnvironment.getInstance().getApcredit().getcourse(tests, scores);
        plan.getTerm(0).getCourses().clear();
        for (Course c : apcourses)
        {
            plan.getTerm(0).getCourses().add(c);
        }

    }

    public JButton getCancelButton() {
        return CancelButton;
    }
}
