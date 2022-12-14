// Copyright @ MyScript. All rights reserved.

package com.example.drawingink;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drawingink.UIReferenceImplementation.ConvertList;
import com.example.drawingink.UIReferenceImplementation.EditorBinding;
import com.example.drawingink.UIReferenceImplementation.EditorData;
import com.example.drawingink.UIReferenceImplementation.EditorView;
import com.example.drawingink.UIReferenceImplementation.FontUtils;
import com.example.drawingink.UIReferenceImplementation.GetViewModel;
import com.example.drawingink.UIReferenceImplementation.InputController;
import com.example.drawingink.UIReferenceImplementation.SmartGuideView;
import com.example.drawingink.databinding.MainActivityBinding;
import com.example.drawingink.iink.ErrorActivity;
import com.example.drawingink.iink.IInkApplication;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.GsonBuilder;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.EditorError;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.MimeType;
import com.myscript.iink.Renderer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Engine engine;
    private ContentPackage contentPackage;
    private ContentPart contentPart;

    private EditorData editorData;
    private EditorView editorView;
    private SmartGuideView smartGuideView;

    private MainActivityBinding binding;
    private GetViewModel getViewModel;

    //bottomSheet
    private AdapterText adapterText;
    private RecyclerView recyclerView;
    private BottomSheetDialog bottomSheetDialog;
    private List<ConvertList> convertList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getViewModel = new ViewModelProvider(this).get(GetViewModel.class);

        ErrorActivity.installHandler(this);





        //bottomsheet
        bottomSheetDialog=new BottomSheetDialog(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet, null, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        engine = IInkApplication.getEngine();
        convertList = new ArrayList<>();
        // configure recognition
        Configuration conf = engine.getConfiguration();
        String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        String tempDir = getFilesDir().getPath() + File.separator + "tmp";
        conf.setString("content-package.temp-folder", tempDir);

        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editorView = findViewById(R.id.editor_view);
        smartGuideView = findViewById(R.id.smart_guide_view);

        // load fonts
        AssetManager assetManager = getApplicationContext().getAssets();
        Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
        editorView.setTypefaces(typefaceMap);

        EditorBinding editorBinding = new EditorBinding(engine, typefaceMap);
        editorData = editorBinding.openEditor(editorView);

        Editor editor = editorData.getEditor();
        setMargins(editor, R.dimen.editor_horizontal_margin, R.dimen.editor_vertical_margin);
        editor.addListener(new IEditorListener() {
            @Override
            public void partChanging(@NonNull Editor editor, ContentPart oldPart, ContentPart newPart) {
                // no-op
            }

            @Override
            public void partChanged(@NonNull Editor editor) {
                invalidateOptionsMenu();
                invalidateIconButtons();
            }

            @Override
            public void contentChanged(@NonNull Editor editor, String[] blockIds) {
                invalidateOptionsMenu();
                invalidateIconButtons();
            }

            @Override
            public void onError(@NonNull Editor editor, @NonNull String blockId, @NonNull EditorError error, @NonNull String message) {
                Log.e(TAG, "Failed to edit block \"" + blockId + "\"" + message);
            }

            @Override
            public void selectionChanged(@NonNull Editor editor) {
                // no-op
            }

            @Override
            public void activeBlockChanged(@NonNull Editor editor, @NonNull String blockId) {
                // no-op
            }
        });

        smartGuideView.setEditor(editor);

        setInputMode(InputController.INPUT_MODE_FORCE_PEN); // If using an active pen, put INPUT_MODE_AUTO here

        String packageName = "File1.iink";
        File file = new File(getFilesDir(), packageName);
        try {
            contentPackage = engine.createPackage(file);
            // Choose type of content (possible values are: "Text Document", "Text", "Diagram", "Math", "Drawing" and "Raw Content")
            contentPart = contentPackage.createPart("Text Document");
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && contentPart != null) {
            actionBar.setTitle(getString(R.string.main_title, contentPart.getType()));
            actionBar.setSubtitle(R.string.app_name);
        } else {
            setTitle(R.string.app_name);
        }

        // wait for view size initialization before setting part
        editorView.post(() -> {
            Renderer renderer = editorView.getRenderer();
            if (renderer != null) {
                renderer.setViewOffset(0, 0);
                editorView.getRenderer().setViewScale(1);
                editorView.setVisibility(View.VISIBLE);
                editor.setPart(contentPart);
            }
        });

        binding.inputModeForcePenButton.setOnClickListener((v) -> setInputMode(InputController.INPUT_MODE_FORCE_PEN));
        binding.inputModeForceTouchButton.setOnClickListener((v) -> setInputMode(InputController.INPUT_MODE_FORCE_TOUCH));
        binding.inputModeAutoButton.setOnClickListener((v) -> setInputMode(InputController.INPUT_MODE_AUTO));
        binding.undoButton.setOnClickListener((v) -> editor.undo());
        binding.redoButton.setOnClickListener((v) -> editor.redo());
        binding.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertList.clear();
                editor.clear();
            }
        });

        binding.editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (convertList == null || convertList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                } else {
                    adapterText.notifyDataSetChanged();
                    bottomSheetDialog.show();
                }


            }
        });

        binding.menuConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);

                if (supportedStates.length > 0) {
                    //editor.convert(null, supportedStates[0]);

                    MimeType[] supportedMimeTypes = editor.getSupportedImportMimeTypes(editor.getRootBlock());
                    // Export a math block to MathML
                    try {
                        String result = editor.export_(editor.getRootBlock(), MimeType.TEXT);
                        Log.d(TAG, "ExportData:" + result);
                            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());


                        ConvertList convertList1 = new ConvertList();
                        convertList1.setText(result);
                        convertList1.setDate(currentDate);
                        convertList.add(convertList1);
                        Log.e("Contextutal", "text>>190>>convertList>>" + new GsonBuilder().setPrettyPrinting().create().toJson(convertList));

                        adapterText=new AdapterText(MainActivity.this,convertList);
                        recyclerView.setAdapter(adapterText);
                        bottomSheetDialog.show();



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    editor.clear();

                }
            }
        });


        invalidateIconButtons();
    }

    @Override
    protected void onDestroy() {
        binding.inputModeForcePenButton.setOnClickListener(null);
        binding.inputModeForceTouchButton.setOnClickListener(null);
        binding.inputModeAutoButton.setOnClickListener(null);
        binding.undoButton.setOnClickListener(null);
        binding.redoButton.setOnClickListener(null);
        binding.clearButton.setOnClickListener(null);

        smartGuideView.setEditor(null);

        Editor editor = editorData.getEditor();
        if (editor != null) {
            editor.getRenderer().close();
            editor.close();
        }
        editorView.setOnTouchListener(null);
        editorView.setEditor(null);

        if (contentPart != null) {
            contentPart.close();
            contentPart = null;
        }
        if (contentPackage != null) {
            contentPackage.close();
            contentPackage = null;
        }

        // IInkApplication has the ownership, do not close here
        engine = null;

        super.onDestroy();
    }

    /*  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.main_activity_menu, menu);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Editor editor = editorData.getEditor();
    if (item.getItemId() == R.id.menu_convert && editor != null && !editor.isClosed())
    {
      ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);
      if (supportedStates.length > 0)
        editor.convert(null, supportedStates[0]);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }*/

    private void setMargins(Editor editor, @DimenRes int horizontalMarginRes, @DimenRes int verticalMarginRes) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Configuration conf = editor.getConfiguration();
        float verticalMarginPX = getResources().getDimension(verticalMarginRes);
        float verticalMarginMM = 25.4f * verticalMarginPX / displayMetrics.ydpi;
        float horizontalMarginPX = getResources().getDimension(horizontalMarginRes);
        float horizontalMarginMM = 25.4f * horizontalMarginPX / displayMetrics.xdpi;
        conf.setNumber("text.margin.top", verticalMarginMM);
        conf.setNumber("text.margin.left", horizontalMarginMM);
        conf.setNumber("text.margin.right", horizontalMarginMM);
        conf.setNumber("math.margin.top", verticalMarginMM);
        conf.setNumber("math.margin.bottom", verticalMarginMM);
        conf.setNumber("math.margin.left", horizontalMarginMM);
        conf.setNumber("math.margin.right", horizontalMarginMM);
    }

    private void setInputMode(int inputMode) {
        editorData.getInputController().setInputMode(inputMode);
        binding.inputModeForcePenButton.setEnabled(inputMode != InputController.INPUT_MODE_FORCE_PEN);
        binding.inputModeForceTouchButton.setEnabled(inputMode != InputController.INPUT_MODE_FORCE_TOUCH);
        binding.inputModeAutoButton.setEnabled(inputMode != InputController.INPUT_MODE_AUTO);
    }

    private void invalidateIconButtons() {
        Editor editor = editorData.getEditor();
        if (editor == null)
            return;
        final boolean canUndo = editor.canUndo();
        final boolean canRedo = editor.canRedo();
        runOnUiThread(() -> {
            binding.undoButton.setEnabled(canUndo);
            binding.redoButton.setEnabled(canRedo);
            binding.clearButton.setEnabled(contentPart != null);
        });
    }
}
