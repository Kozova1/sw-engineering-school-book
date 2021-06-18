// This is in MainActivity.onOptionsItemSelected()
ExerciseRepository exRepo = new ExerciseRepository(getApplication()); // Get all exercises
exRepo.getExercises().observe(this, exercises -> {
// no indentation to save space
ArticleRepository arRepo = new ArticleRepository(getApplication()); // Get all articles
arRepo.getArticles().observe(this, articles -> {
    Gson gson = new Gson(); // JSON encoder
    Course holder = new Course(articles, exercises);
    String jsonResult = gson.toJson(holder); // encode Course as JSON
    // Generate random ID of 8 digits
    StringBuilder randomId = new StringBuilder();
    Random prng = new Random();
    for (int i = 0; i < 8; i++) {
        randomId.append(prng.nextInt(10));
    }
    String id = randomId.toString();
    // Upload JSON to relevant Firebase node
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(id);
    ref.setValue(jsonResult);
    ClipboardManager mgr = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    // Copy the ID to clipboard
    mgr.setPrimaryClip(ClipData.newPlainText("Course ID", id));
    Snackbar snack = Snackbar.make(
            drawer,
            "Course published with ID: "
                + id
                + ". Copied to clipboard.",
                BaseTransientBottomBar.LENGTH_LONG
    ); // Alert user
    snack.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            finishCourse();
            // When snackbar closes, move to FirstTimeActivity
            super.onDismissed(transientBottomBar, event);
        }
    });
    snack.show();
});
});
