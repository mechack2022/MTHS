#!/bin/bash

cd /Users/MAC/Desktop/personal-project/MTHS/backend

# Update imports in all Java files
find src/main/java/com/mths -type f -name "*.java" -exec sed -i '' \
  -e 's|import com\.mths\.controller\.|import com.mths.auth.controller.|g' \
  -e 's|import com\.mths\.service\.|import com.mths.auth.service.|g' \
  -e 's|import com\.mths\.repository\.|import com.mths.auth.repository.|g' \
  -e 's|import com\.mths\.entity\.|import com.mths.auth.entity.|g' \
  -e 's|import com\.mths\.dto\.|import com.mths.auth.dto.|g' \
  -e 's|import com\.mths\.config\.|import com.mths.shared.config.|g' \
  -e 's|import com\.mths\.security\.|import com.mths.shared.security.|g' \
  -e 's|import com\.mths\.jwt\.|import com.mths.shared.jwt.|g' \
  -e 's|import com\.mths\.utils\.|import com.mths.shared.utils.|g' \
  -e 's|import com\.mths\.exceptions\.|import com.mths.shared.exceptions.|g' \
  -e 's|import com\.mths\.mapper\.|import com.mths.shared.mapper.|g' \
  -e 's|import com\.mths\.constants\.|import com.mths.shared.constants.|g' \
  -e 's|import com\.mths\.websocket\.|import com.mths.shared.websocket.|g' \
  {} \;

echo "Imports updated - first pass completed"
