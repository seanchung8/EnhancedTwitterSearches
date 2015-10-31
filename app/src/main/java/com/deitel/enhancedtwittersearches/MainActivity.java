// MainActivity.java
// Manages your favorite Twitter searches for easy  
// access and display in the device's web browser
package com.deitel.enhancedtwittersearches;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends Activity implements ListViewFragment.OnItemListSelectedListener,
        WebViewFragment.OnWebViewFragmentListener {

   private EditText queryEditText; // EditText where user enters a query
   private EditText tagEditText; // EditText where user tags a query

   private CheckBox liveCheckBox;
   private CheckBox newsCheckBox;
   private CheckBox photosCheckBox;
   private CheckBox videosCheckBox;

   private ListViewFragment listViewFragment;

   // called when MainActivity is first created
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

      setContentView(R.layout.activity_main);

      insertListViewFragment();

      // get references to the EditTexts  
      queryEditText = (EditText) findViewById(R.id.queryEditText);
      tagEditText = (EditText) findViewById(R.id.tagEditText);

      // register listener to save a new or edited search
      ImageButton saveButton =
         (ImageButton) findViewById(R.id.saveButton);
      saveButton.setOnClickListener(saveButtonListener);

      // setup the checkboxes for search filter
      liveCheckBox = (CheckBox) findViewById(R.id.liveCheckBox);
      newsCheckBox = (CheckBox) findViewById(R.id.newsCheckBox);
      photosCheckBox = (CheckBox) findViewById(R.id.photosCheckBox);
      videosCheckBox = (CheckBox) findViewById(R.id.videosCheckBox);

   } // end method onCreate

   @Override
   public void onBackPressed() {
      // we need to handle the back button pressed event
      // as we are using a custom WebViewClient class

      // check the backstack. If it is not empty, pop it
      if (getFragmentManager().getBackStackEntryCount() > 0) {
         getFragmentManager().popBackStack();
         return;
      }

      super.onBackPressed();
   }

   private void insertListViewFragment() {
      listViewFragment = new ListViewFragment();

      FragmentManager fm = getFragmentManager();
      FragmentTransaction fragmentTransaction = fm.beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, listViewFragment);
      fragmentTransaction.commit();
   }

   // saveButtonListener saves a tag-query pair into SharedPreferences
   public OnClickListener saveButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(View v) 
      {
         // create tag if neither queryEditText nor tagEditText is empty
         if (queryEditText.getText().length() > 0 &&
            tagEditText.getText().length() > 0)
         {
            // actual saving done by ListViewFragment
            listViewFragment.addTaggedSearch(queryEditText.getText().toString(),
               tagEditText.getText().toString());

            queryEditText.setText(""); // clear queryEditText
            tagEditText.setText(""); // clear tagEditText
            
            ((InputMethodManager) getSystemService(
               Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
               tagEditText.getWindowToken(), 0);  
         } 
         else // display message asking user to provide a query and a tag
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = 
               new AlertDialog.Builder(MainActivity.this);

            // set dialog's message to display
            builder.setMessage(R.string.missingMessage);
            
            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.OK, null); 
            
            // create AlertDialog from the AlertDialog.Builder
            AlertDialog errorDialog = builder.create();
            errorDialog.show(); // display the modal dialog
         } 
      } // end method onClick
   }; // end OnClickListener anonymous inner class

   @Override
   public void onItemSelectedInteraction(String id) {
      // instantiate simple fragment using factory method
      WebViewFragment webViewFragment = WebViewFragment.newInstance(id);

      FragmentTransaction ft = getFragmentManager().beginTransaction();

      // perform card flip animation
      ft.setCustomAnimations(
              R.anim.card_flip_left_in, R.anim.card_flip_left_out, R.anim.card_flip_right_in, R.anim.card_flip_right_out);

      ft.replace(R.id.fragment_container, webViewFragment);
      ft.addToBackStack(null);
      ft.commit();

   }

   // build the twitter search string based on the tag and selected filter(s)
    public String buildSearchString(String topic) {

        boolean isDefaultFilter = liveCheckBox.isChecked() && newsCheckBox.isChecked() &&
                photosCheckBox.isChecked() && videosCheckBox.isChecked();

       String twitterURL = getString(R.string.searchURL2);
       if (isDefaultFilter) {
            return twitterURL + "?q=" + topic;
        }

        StringBuilder filterString = new StringBuilder("?");

        if (liveCheckBox.isChecked())
            filterString.append("f=tweets&");

        if (newsCheckBox.isChecked())
            filterString.append("f=news&");

        if (photosCheckBox.isChecked())
            filterString.append("f=images&");

        if (videosCheckBox.isChecked())
            filterString.append("f=videos&");

        return twitterURL + filterString.toString() + "q=" + topic;
    }

   // populate the edit text and tag for editing
   @Override
   public void editTaggedSearch(String tag, String topic) {
      tagEditText.setText(tag);
      queryEditText.setText(topic);
   }


   // Display or hide hour glass in the toolbar
   @Override
   public void showHourGlass(boolean show) {
      setProgressBarIndeterminateVisibility(show);
   }


} // end class MainActivity


/**************************************************************************
 * (C) Copyright 1992-2014 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/