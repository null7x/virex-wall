# Google Play Billing Setup

## Prerequisites

1. Google Play Developer Account ($25 one-time fee)
2. App uploaded to Google Play Console (at least internal testing track)

## Step 1: Create App in Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Click "Create app"
3. Fill in:
   - App name: `VIREX Wallpapers`
   - Default language: English (US)
   - App or game: App
   - Free or paid: Free
4. Accept Developer Program Policies
5. Click "Create app"

## Step 2: Upload Initial Build

Before you can create in-app products, you must upload at least one APK/AAB:

1. Build release AAB:
   ```bash
   .\gradlew.bat bundleRelease
   ```
2. Go to "Testing" → "Internal testing"
3. Click "Create new release"
4. Upload `app/build/outputs/bundle/release/app-release.aab`
5. Add release notes
6. Save and roll out to internal testing

## Step 3: Create In-App Product

1. Go to "Monetize" → "In-app products"
2. Click "Create product"
3. Fill in:
   - **Product ID**: `virex_pro_unlock` *(must match code exactly)*
   - **Name**: VIREX PRO
   - **Description**: Unlock all premium features including unlimited downloads, offline access, and no watermarks
   - **Default price**: $2.99
4. Set status to "Active"
5. Click "Save"

## Step 4: Configure License Testing

For testing purchases without being charged:

1. Go to "Settings" → "License testing"
2. Add email addresses of testers
3. Set "License response" to "RESPOND_NORMALLY"
4. Save

## Step 5: Test Purchases

1. Install app on device with tester Google account
2. Navigate to PRO upgrade screen
3. Complete purchase flow
4. Verify:
   - PRO badge appears
   - Premium wallpapers unlock
   - Purchases persist after app restart

## Important Notes

### Product ID
The product ID `virex_pro_unlock` is defined in:
- [BillingRepository.kt](app/src/main/java/com/virex/wallpapers/data/repository/BillingRepository.kt) - `PRO_PRODUCT_ID`

If you change the product ID, update both places!

### Testing vs Production
- Test purchases are refunded automatically after 14 days
- Test users must be added to license testing
- Test device must use same Google account

### Handling Edge Cases
The app handles:
- Network errors during purchase
- Pending purchases
- Already owned products
- Restore purchases for reinstalls

## Pricing Strategy Recommendations

| Tier | Price | Notes |
|------|-------|-------|
| Entry | $1.99 | High conversion, lower ARPU |
| Standard | $2.99 | Recommended starting point |
| Premium | $4.99 | Higher ARPU, lower conversion |

Consider regional pricing for countries with lower purchasing power.

## Analytics Integration

The app fires these events (via Firebase Analytics):
- `pro_screen_viewed` - User opened PRO upgrade screen
- `pro_purchase_started` - User initiated purchase
- `pro_purchase_success` - Purchase completed
- `pro_purchase_restored` - Restored previous purchase

Track these in Firebase Console to optimize conversion.

## Troubleshooting

### "This version of the app is not configured for billing"
- Make sure app is signed with the same key as uploaded to Play Console
- Version code must match or be lower than uploaded version
- Wait 15-30 minutes after uploading new version

### Purchase stuck on "Processing"
- Check internet connection
- Ensure Google Play Services is up to date
- Try clearing Google Play Store cache

### Cannot see product
- Verify product ID matches exactly
- Product must be "Active" status
- App must be published to at least one track
- Wait 15 minutes after creating product

### Test purchases not working
- Verify tester email is in license testing
- Device must be signed into that Google account
- Clear Play Store data and cache

## Production Checklist

Before launching:

- [ ] Product ID matches in code and console
- [ ] Product is Active status
- [ ] All testers verified purchase flow works
- [ ] Analytics events firing correctly
- [ ] Error handling tested (no internet, etc.)
- [ ] Privacy policy mentions in-app purchases
- [ ] Store listing mentions PRO features
