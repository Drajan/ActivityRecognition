package com.gingerio.activitydetect;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;




public class Instructions extends Activity implements OnClickListener{

	Button enableDone;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	// Setup the window	    
    	setContentView(R.layout.readme);
    	enableDone = (Button) findViewById(R.id.done);
    	enableDone.setOnClickListener(this);
	}

		/*	public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which){
				case DialogInterface.BUTTON_POSITIVE:
					finish();
					break;
				
				case DialogInterface.BUTTON_NEGATIVE:
				dialog.cancel();
				break;
				
				default:
					break;
				}
			}*/   //DialogInterface.OnClickListener,

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(v == enableDone)
					finish();
			}
    	
		
	
}
