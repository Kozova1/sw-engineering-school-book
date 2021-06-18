public class ExerciseFragment extends Fragment {
  private int id;
  private String test;
  // result of code goes here
  private TextView resultsView;
  // code goes here
  private EditText codeView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(
            R.layout.fragment_view_exercise, container, false
    );
    Bundle args = requireArguments();


    String instructions = args.getString("instructions");
    String template = args.getString("template");
    this.test = args.getString("test");
    this.id = args.getInt("id");

    this.resultsView = view.findViewById(R.id.exerciseResults);

    TextView instructionView = view.findViewById(R.id.exerciseViewerInstructions);
    final Prism4j prism4j = new Prism4j(new AppGrammarLocator());

    final Markwon markwon = Markwon.builder(view.getContext())
            .usePlugin(
                    SyntaxHighlightPlugin.create(
                        prism4j,
                        Prism4jThemeDarkula.create()
                    )
            ).build();
    // Use Markwon library to render markdown in instructionView
    // Use Prism4j for syntax highlighting of code
    markwon.setMarkdown(instructionView, instructions);
    codeView = view.findViewById(R.id.code_editor);
    codeView.setText(template);

    view.findViewById(R.id.exerciseFAB).setOnClickListener(v -> {
      // Ugly hack to prevent keyboard hiding snackbar
      InputMethodManager imm = (InputMethodManager) (v
          .getContext()
          .getSystemService(Activity.INPUT_METHOD_SERVICE));
      imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

      resultsView.setText(""); // clear error screen
      // Standard library
      // Java interop library *is* available here.
      // be careful!
      Globals env = JsePlatform.standardGlobals();
      Snackbar message;
      LuaValue res;
      boolean result;
      try {
        // Load code and call resulting value
        // Thus running the loaded code.
        env.load(this.codeView.getText().toString()).call();
      } catch (LuaError e) {
        // Detected error in our code!
        resultsView.setText(e.getLocalizedMessage());
        message = Snackbar.make(
                v,
                "Exception encountered when loading your code.",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        message.show();
        return;
      }
      try {
        // Load test code and call resulting value
        // This code makes sure the solution is correct
        env.load(this.test).call();
      } catch (LuaError e) {
        // Test code is invalid!
        resultsView.setText(e.getLocalizedMessage());
        message = Snackbar.make(
                v,
                "Exception encountered when loading the test code.",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        message.show();
        return;
      }
      try {
        // this actually checks the solution
        res = env.get("test").call();
      } catch (LuaError e) {
        // Test code crashed!
        resultsView.setText(e.getLocalizedMessage());
        message = Snackbar.make(
                v,
                "Test did not pass. Try again!",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        message.show();
        return;
      }

      try {
        // check if solution is correct
        result = res.checkboolean();
      } catch (LuaError e) {
        // Test is invalid!
        resultsView.setText(e.getLocalizedMessage());
        message = Snackbar.make(
                v,
                "Test did not return a boolean.",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        message.show();
        return;
      }

      if (result) {
        // Solution is correct!
        message = Snackbar.make(
                v,
                "Test passed. Success!",
                BaseTransientBottomBar.LENGTH_SHORT
        );
        ExerciseRepository repo = new ExerciseRepository(
                requireActivity().getApplication()
        );
        repo.markAsDone(this.id, true, this.codeView.getText().toString());
      } else {
        // Solution is incorrect!
        message = Snackbar.make(
                v,
                "Test did not pass. Try again!",
                BaseTransientBottomBar.LENGTH_SHORT
        );
      }
      message.show();
    });
    return view;
  }
}
