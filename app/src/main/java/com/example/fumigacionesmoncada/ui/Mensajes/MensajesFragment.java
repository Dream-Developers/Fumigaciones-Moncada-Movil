package com.example.fumigacionesmoncada.ui.Mensajes;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fumigacionesmoncada.ChatActivity;
import com.example.fumigacionesmoncada.ChatApplication;
import com.example.fumigacionesmoncada.ChatsRecent;
import com.example.fumigacionesmoncada.ContactsActivity;
import com.example.fumigacionesmoncada.NavegacionAdministradorActivity;
import com.example.fumigacionesmoncada.R;
import com.example.fumigacionesmoncada.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

public class MensajesFragment extends Fragment   implements Application.ActivityLifecycleCallbacks {

    private FloatingActionButton add_chat;
    private GroupAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mensajes, container, false);

        RecyclerView rv = view.findViewById(R.id.recycler_contact);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));


        getActivity().getApplication().registerActivityLifecycleCallbacks(this);


        //<!--android:name=".ChatApplication"-->


        adapter = new GroupAdapter();
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {

               // Toast.makeText(getContext(), "Gracias por darle click, aun no funciona", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                ContactoItem contactoItem = (ContactoItem) item;

                intent.putExtra("uuid", contactoItem.contactos.getUuid());
                intent.putExtra("name", contactoItem.contactos.getUsername());
                intent.putExtra("photo", contactoItem.contactos.getPhotoUrl());

                startActivity(intent);
            }
        });



        add_chat = view.findViewById(R.id.add_chat);

        add_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                startActivity(intent);
            }
        });


        updateToken();
        searchLastMensaje();


        return view;
    }

    private void updateToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .update("token", token);
        }
    }

    private void searchLastMensaje() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;


        FirebaseFirestore.getInstance().collection("/last-messages")
                .document(uid)
                .collection("contacts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if (documentChanges != null) {
                            for (DocumentChange doc: documentChanges) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    ChatsRecent contact = doc.getDocument().toObject(ChatsRecent.class);

                                    adapter.add(new ContactoItem(contact));
                                }
                            }
                        }
                    }
                });


    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        setOnline(true);
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        setOnline(false);
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {

    }


    private void setOnline(boolean enabled) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .update("online", enabled);
        }
        Toast.makeText(getActivity(), "Si da", Toast.LENGTH_LONG).show();
    }

    public  class ContactoItem extends Item<ViewHolder>{

        private final ChatsRecent contactos;

        public ContactoItem(ChatsRecent contactos) {
            this.contactos = contactos;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView contactname = viewHolder.itemView.findViewById(R.id.textView);
            TextView mensaje = viewHolder.itemView.findViewById(R.id.textView2);
            ImageView imgPhoto = viewHolder.itemView.findViewById(R.id.imageView);


            contactname.setText(contactos.getUsername());
            mensaje.setText(contactos.getLastMessage());
            Picasso.get()
                    .load(contactos.getPhotoUrl())
                    .into(imgPhoto);
        }

        @Override
        public int getLayout() {
            return R.layout.item_contactos_mensajes;
        }
    }


}