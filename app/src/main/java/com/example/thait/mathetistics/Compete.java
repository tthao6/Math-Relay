package com.example.thait.mathetistics;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Compete extends AppCompatActivity {

    TextView username,score;
    public static List<Question> tempQList;
    public static List<Question> QList;
    public static List<User> topUsers;
    public static double quizScore;
    Firebase ref = new Firebase(Constants.FIREBASE_URL);
    public static int qnum = 0;

    @Override public void onBackPressed(){
        startActivity(new Intent(this,Choice.class));
    }


    public void mainMenuClicked(View v) {
        Intent i = new Intent(this,Choice.class);
        startActivity(i);
    }

    public void logoutC(View v){
        ref.unauth();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void ranked(View v){
        Intent intent = new Intent(this,Rank.class);
        startActivity(intent);
    }

    public void addQuestion(View v){
        Intent intent = new Intent(this,QRegistration.class);
        startActivity(intent);
    }

//    public void Problems(View v){
//        QList = new ArrayList<Question>();
//        long seed = System.nanoTime();
//        if(tempQList != null) {
//            int i = 1;
//            Collections.shuffle(tempQList, new Random(seed));
//            Iterator<Question> itr = tempQList.iterator();
//            while(itr.hasNext() && i < 8) {
//                    QList.add(itr.next());
//                    i++;
//            }
//            quizScore = 0;
//            Intent intent = new Intent(this, Exercise.class);
//            startActivity(intent);
//        }
//    }

    public void playClicked(View v) {
        Intent i = new Intent(this, Directions.class);
        startActivity(i);
    }

    public static int nextQ() {
        qnum++;
        return qnum;
    }
    public static void resetQ() {
        qnum = 0;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compete);
        username = (TextView)findViewById(R.id.textView2);
        score = (TextView)findViewById(R.id.textView3);
        tempQList = new ArrayList<Question>();
        Firebase ref = new Firebase(Constants.FIREBASE_URL);
        final Firebase refName = ref.child("users").child(ref.getAuth().getUid()).child("username");
        final Firebase refScore = ref.child("users").child(ref.getAuth().getUid()).child("score");
        final Firebase qRef = ref.child("Questions");

        topUsers = new ArrayList<User>();
        addingUsers();

        qRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map1 = dataSnapshot.getValue(Map.class);
                List<String> qList = getList(map1);
                Iterator<String> itr = qList.iterator();
                while(itr.hasNext()){
                    String next = itr.next();
                    Firebase infoRef = qRef.child(next);
                    infoRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String,Object> map2 = dataSnapshot.getValue(Map.class);
                            if(map2 != null){
                                //List<Object> newList = getValue(map2);
                                Question tempQ = makeQuestion(map2);
                                if(tempQ != null){
                                    tempQList.add(tempQ);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        refScore.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stringScore = dataSnapshot.getValue(String.class);
                score.setText("High Score: " + stringScore);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.print("Error in Scored");
            }
        });

        refName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String theUsername = dataSnapshot.getValue(String.class);
                username.setText("Hello " + theUsername + "!");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.print("Error in Username");
            }
        });
    }

    private List<String> getList(Map<String,Object> map){
        List<String> tempList = new ArrayList<String>();

        if(map != null){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                tempList.add(entry.getKey());
            }
        }

        return tempList;
    }

    private Question makeQuestion(Map<String,Object> map){

        String ans1 = (String)map.get("ans1");
        String ans2 = (String)map.get("ans2");
        String ans3 = (String)map.get("ans3");
        String ans4 = (String)map.get("ans4");
        String cAns = (String)map.get("correctAns");
        String q = (String)map.get("theQuestion");

        if(ans1 == null || ans2 == null || ans3 == null || ans4 == null || cAns == null || q == null){
            return null;
        }
        Question tempQ = new Question(q,ans1,ans2,ans3,ans4,cAns);
        return tempQ;
    }


    public void addingUsers(){
        final Firebase userIdRef = ref.child("users");
        userIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> firstMap = dataSnapshot.getValue(Map.class);
                List<String> tempList = new ArrayList<String>();
                if(firstMap != null){
                    for (Map.Entry<String, Object> entry : firstMap.entrySet()) {
                        tempList.add(entry.getKey());
                    }
                }

                for(int i = 0; i < tempList.size(); i++){
                    final Firebase userRef = ref.child("users").child(tempList.get(i));
                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String,Object> secondMap = dataSnapshot.getValue(Map.class);
                            if(secondMap != null){
                                String name = (String)secondMap.get("username");
                                String strScore = (String)secondMap.get("score");
                                double userScore = Double.parseDouble(strScore);
                                User tempUser = new User(name,userScore);
                                final int listSize = topUsers.size();
                                if(topUsers.size() == 0 || topUsers.isEmpty() || userScore == 0) {
                                    topUsers.add(tempUser);
                                }
                                else{
                                    for(int i = 0; i < listSize;i++){
                                        if(topUsers.get(i).compareTo(tempUser) == -1){
                                            topUsers.add(i,tempUser);
                                            break;
                                        }
                                        else if(i == listSize -1){
                                            topUsers.add(tempUser);
                                        }
                                    }
                                }
                                userRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }


                userIdRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

}
