package de.felixnuesse.timedsilence.fragments

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import de.felixnuesse.timedsilence.Constants
import de.felixnuesse.timedsilence.Constants.Companion.APP_NAME
import de.felixnuesse.timedsilence.R
import de.felixnuesse.timedsilence.handler.WifiHandler
import de.felixnuesse.timedsilence.model.data.WifiObject
import de.felixnuesse.timedsilence.model.database.DatabaseHandler
import de.felixnuesse.timedsilence.ui.WifiListAdapter
import kotlinx.android.synthetic.main.wifi_connected_fragment.*


class WifiConnectedFragment : Fragment() {

    companion object {
        fun newInstance() = WifiConnectedFragment()
    }

    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var viewModel: WifiConnectedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return  inflater.inflate(R.layout.wifi_connected_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WifiConnectedViewModel::class.java)
        // TODO: Use the ViewModel
        checkContainer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        buttonRequestWifiPermissions.setOnClickListener {
            Log.e(Constants.APP_NAME, "WifiConnectedFragment: Request Location Permission!")
            WifiHandler.requestPermissions(view.context as Activity)
            checkContainer()
        }

        button_wifi_add_fragment.setOnClickListener {
            Log.e(APP_NAME, "WifiFragment: Add new!")
            createSSIDDialog(view.context)
        }

        val db = DatabaseHandler(view.context)

        Log.e(Constants.APP_NAME, "WifiFragment: DatabaseResuluts: Size: "+db.getAllWifiEntries().size)

        viewManager = LinearLayoutManager(view.context)
        viewAdapter = WifiListAdapter(db.getAllWifiEntries())

        wifi_recylcer_list_view.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(false)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

    }



    private fun checkContainer(){

        //val a = activity.findViewById(R.id.WifiContentContainer) as ConstraintLayout
        val a = WifiContentContainer
        //val b = activity.findViewById(R.id.WifiRequestLocationContainer) as ConstraintLayout
        val b = WifiRequestLocationContainer


        a.visibility = View.GONE
        b.visibility = View.VISIBLE

        if(WifiHandler.grantedWifiPermission(this.context as Activity)){
            a.visibility = View.VISIBLE
            b.visibility = View.GONE
        }
    }


    fun createSSIDDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("SSID")

        // Set up the input
        val input = EditText(context)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->
                createTypeDialog(context, input.text.toString())

            })
        builder.setNegativeButton(R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    fun createTypeDialog(context: Context, ssid: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Type 1,2")

        // Set up the input
        val input = EditText(context)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton(R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->

                val db = DatabaseHandler(context)
               // db.createWifiEntry(WifiObject(0,ssid, Integer.valueOf(input.text.toString())))
                //todo Add proper ui so that the user can add searching or different stuff to it
                db.createWifiEntry(WifiObject(0,ssid, 1))
                viewAdapter = WifiListAdapter(db.getAllWifiEntries())

                wifi_recylcer_list_view.apply {
                    // use this setting to improve performance if you know that changes
                    // in content do not change the layout size of the RecyclerView
                    setHasFixedSize(true)

                    // use a linear layout manager
                    layoutManager = viewManager

                    // specify an viewAdapter (see also next example)
                    adapter = viewAdapter

                }


            })
        builder.setNegativeButton(R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }
}
