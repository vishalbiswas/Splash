package net.ddns.vishalbiswas.splash;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class UserDisplayFragment extends Fragment {
    private static ImageView fragPic;
    private static TextView fragUser;
    private static TextView fragEmail;

    private OnFragmentInteractionListener mListener;

    public UserDisplayFragment() {
        // Required empty public constructor
    }

    public static void updateViews() {
        String username = GlobalFunctions.defaultIdentity.getUsername();
        String name;
        if (GlobalFunctions.defaultIdentity.getFirstname().isEmpty() && GlobalFunctions.defaultIdentity.getLastname().isEmpty()) {
            name = username;
        } else {
            name = String.format("%s %s", GlobalFunctions.defaultIdentity.getFirstname(), GlobalFunctions.defaultIdentity.getLastname());
        }
        String email = GlobalFunctions.defaultIdentity.getEmail();
        Bitmap profpic = GlobalFunctions.defaultIdentity.getProfpic();
        if (profpic != null) {
            assert fragPic != null;
            fragPic.setImageBitmap(profpic);
        }

        assert fragUser != null;
        fragUser.setText(name);

        assert fragEmail != null;
        fragEmail.setText(email);
    }

    public UserDisplayFragment newInstance() {
        return new UserDisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null) {
            fragPic = (ImageView) getView().findViewById(R.id.fragPic);
            fragUser = (TextView) getView().findViewById(R.id.fragUser);
            fragEmail = (TextView) getView().findViewById(R.id.fragEmail);
            updateViews();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_display, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if ((context instanceof OnFragmentInteractionListener)) {
            mListener = (OnFragmentInteractionListener) context;
        } /*else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
