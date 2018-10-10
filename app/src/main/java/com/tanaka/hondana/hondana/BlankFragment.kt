package com.tanaka.hondana.hondana

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "isbn"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BlankFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BlankFragment : Fragment() {
    // TODO: Rename and change types of parameters
    var isbn: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isbn = it.getString(ARG_PARAM1)
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Log.d("tag", isbn)
        val v = inflater.inflate(R.layout.fragment_blank, container, false)
        applyData(v, isbn!!)
        Log.d("tag", "applyまでOK")

        v.findViewById<Button>(R.id.buttonBorrow).setOnClickListener{
            //v.Toast.makeText(this, "借りました！", Toast.LENGTH_SHORT).show()
            //finish()
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().remove(this).commit()
        }

        v.findViewById<Button>(R.id.buttonReturn).setOnClickListener{
            //Toast.makeText(this, "返しました！", Toast.LENGTH_SHORT).show()
            fragmentManager!!.beginTransaction().remove(this).commit()
        }
        return v

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun applyData(v: View, isbn: String){
        val bookstock = BookStock(isbn)
        val bookinfo = BookInfo(isbn)
        v.findViewById<TextView>(R.id.textIsbn).text = isbn
        v.findViewById<TextView>(R.id.textTitle).text = bookinfo.title!!
        v.findViewById<TextView>(R.id.textAuthor).text = bookinfo.author!!
        v.findViewById<TextView>(R.id.numberAll).text = bookstock.numberAll!!.toString() + getString(R.string.book_unit)
        v.findViewById<TextView>(R.id.numberOnloan).text = bookstock.numberOnloan!!.toString() + getString(R.string.book_unit)
        v.findViewById<TextView>(R.id.numberAvailable).text = bookstock.numberAvailable!!.toString() + getString(R.string.book_unit)
        v.findViewById<Button>(R.id.buttonBorrow).isEnabled = bookstock.canBorrow
        v.findViewById<Button>(R.id.buttonReturn).isEnabled = bookstock.canReturn!!
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                BlankFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
