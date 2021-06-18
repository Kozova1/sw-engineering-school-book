// From FirstTimeActivity.onCreate()
joinCourseBtn.setOnClickListener(v -> {
    // If enough characters were typed
    // EditText max length keeps this safe
    if (joinCourseId.getText().length() == 8) {
        String courseId = joinCourseId.getText().toString();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(courseId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // If no such course exists, it's an invalid ID
                    Snackbar.make(
                            v,
                            "Invalid Course ID!",
                            BaseTransientBottomBar.LENGTH_LONG
                    ).show();
                    return;
                }
                // Get JSON code from Firebase
                String asJson = snapshot.getValue(String.class);
                // This internally decodes the JSON
                Course course = new Course(asJson);
                // We don't want to insert in the UI thread
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    ArticleDao articleDao = AppDatabase
                        .getDatabase(act)
                        .articleDao();
                    ExerciseDao exerciseDao = AppDatabase
                        .getDatabase(act)
                        .exerciseDao();
                    // Delete any leftovers
                    articleDao.clear();
                    exerciseDao.clear();
                    // Insert everything decoded from JSON
                    articleDao.insertAll(course.articles);
                    exerciseDao.insertAll(course.exercises);
                });
                // write to sharedpreferences
                beginCourse(getApplicationContext());
                // go to MainActivity
                moveToNextActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Couldn't access course for some reason
                Snackbar.make(
                        v,
                        "Invalid Course ID!",
                        BaseTransientBottomBar.LENGTH_LONG
                ).show();
            }
        });
        ref.get();
    } else {
        // not enough letters in course ID
        Snackbar.make(
                v,
                "Course ID is exactly 8 digits long",
                BaseTransientBottomBar.LENGTH_LONG
        ).show();
    }
});
