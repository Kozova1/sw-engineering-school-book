public class ReplFragment extends Fragment implements AutoCloseable {

  private final Globals globalEnv;
  // Output for Lua VM
  private final ByteArrayOutputStream stdout;
  private final PrintStream pOStream;
  // Input for Lua VM
  private final PipedInputStream stdin;
  private final PipedOutputStream stdinWriteStream;
  // Input box, both for code and stdin
  private EditText inputBox;
  // Output "terminal"
  private TextView textView;

  public ReplFragment() throws IOException {
    // create and connect stdin
    stdin = new PipedInputStream();
    stdinWriteStream = new PipedOutputStream();
    stdin.connect(stdinWriteStream);
    // If this fails, no reason to continue
    stdout = new ByteArrayOutputStream();
    try {
      pOStream = new PrintStream(stdout, true, "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException();
    }
    // load Lua VM Standard library
    this.globalEnv = JsePlatform.standardGlobals();
    // redirect STDIN, STDERR, STDOUT
    globalEnv.STDOUT = pOStream;
    globalEnv.STDERR = pOStream;
    globalEnv.STDIN = stdin;
    // Remove Lua VM Java interop library for security
    globalEnv.set("java", LuaValue.NIL);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_repl, container, false);

    textView = view.findViewById(R.id.repl_history_target);
    inputBox = view.findViewById(R.id.repl_code_in);

    view.findViewById(R.id.repl_send).setOnClickListener(v -> {
      // get input code
      String inputCode = inputBox.getText().toString();
      inputBox.setText("");
      try {
        // load input code
        // this returns a function,
        // which when called executes the code
        LuaValue res = globalEnv.load(inputCode).call();
        // it = return value of the code
        globalEnv.set("it", res);
        // if it != nil print it
        if (!res.isnil())
          stdout.write(("it = " + res.tojstring())
                  .getBytes(StandardCharsets.UTF_8));
        stdout.write('\n');
      } catch (LuaError | IOException e) {
        // Lua code can contain errrors, don't want to crash
        try {
          stdout.write(e.getLocalizedMessage()
                  .getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
          ioException.printStackTrace();
        }
      } finally {
        stdout.write('\n');
        textView.setText(
                new String(stdout.toByteArray(), StandardCharsets.UTF_8)
        );
      }
    });

    return view;
  }

  @Override
  public void close() throws Exception {
    // autoclose the streams
    // usage:
    // try(ReplFragment repl = new ReplFragment()) {
    //      ...
    // }
    stdout.close();
    pOStream.close();
    stdin.close();
    stdinWriteStream.close();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    try {
      close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
