#!/bin/bash

cd /Users/MAC/Desktop/personal-project/MTHS/backend

# Update package declarations for auth
find src/main/java/com/mths/auth -type f -name "*.java" | while read file; do
  newpkg=$(echo "$file" | sed 's|.*/com/mths/auth/\([^/]*\)/.*|auth.\1|')
  sed -i '' "s|^package com\.mths;|package com.mths.$newpkg;|" "$file"
done

# Update package declarations for consultation
find src/main/java/com/mths/consultation -type f -name "*.java" | while read file; do
  newpkg=$(echo "$file" | sed 's|.*/com/mths/consultation/\([^/]*\)/.*|consultation.\1|')
  sed -i '' "s|^package com\.mths;|package com.mths.$newpkg;|" "$file"
done

# Update package declarations for hospital
find src/main/java/com/mths/hospital -type f -name "*.java" | while read file; do
  newpkg=$(echo "$file" | sed 's|.*/com/mths/hospital/\([^/]*\)/.*|hospital.\1|')
  sed -i '' "s|^package com\.mths;|package com.mths.$newpkg;|" "$file"
done

# Update package declarations for patient
find src/main/java/com/mths/patient -type f -name "*.java" | while read file; do
  newpkg=$(echo "$file" | sed 's|.*/com/mths/patient/\([^/]*\)/.*|patient.\1|')
  sed -i '' "s|^package com\.mths;|package com.mths.$newpkg;|" "$file"
done

# Update package declarations for shared
find src/main/java/com/mths/shared -type f -name "*.java" | while read file; do
  newpkg=$(echo "$file" | sed 's|.*/com/mths/shared/\([^/]*\)/.*|shared.\1|')
  sed -i '' "s|^package com\.mths;|package com.mths.$newpkg;|" "$file"
done

echo "Package declarations updated successfully"
