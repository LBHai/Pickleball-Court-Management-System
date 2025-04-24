package UI.Component;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SEP490.G9.R;

public class PermissionRequestDialog extends DialogFragment {

    private static final int PERMISSION_REQUEST_CODE = 100;

    // Xác định các quyền cần thiết tùy theo phiên bản Android
    private static List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();

        // Quyền camera luôn cần
        permissions.add(Manifest.permission.CAMERA);

        // Quyền đọc bộ nhớ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            // WRITE_EXTERNAL_STORAGE cũng cần cho việc truy cập ảnh trên Android < 10
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // Quyền thông báo chỉ cần cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        return permissions;
    }

    // Map để lưu trữ mô tả của mỗi quyền
    private static final Map<String, String> PERMISSION_DESCRIPTIONS = new HashMap<>();

    // Khởi tạo mô tả cho mỗi quyền
    static {
        PERMISSION_DESCRIPTIONS.put(Manifest.permission.CAMERA,
                "Để mở máy ảnh chụp hình và quét mã QR");
        PERMISSION_DESCRIPTIONS.put(Manifest.permission.READ_EXTERNAL_STORAGE,
                "Để truy cập và đọc ảnh từ bộ nhớ thiết bị");
        PERMISSION_DESCRIPTIONS.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "Để lưu trữ ảnh và dữ liệu vào bộ nhớ thiết bị");
        PERMISSION_DESCRIPTIONS.put(Manifest.permission.POST_NOTIFICATIONS,
                "Để gửi thông báo về các cập nhật và thông tin quan trọng");

        // Thêm cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSION_DESCRIPTIONS.put(Manifest.permission.READ_MEDIA_IMAGES,
                    "Để đọc và truy cập ảnh từ bộ nhớ thiết bị");
        }
    }

    private int currentPermissionIndex = 0;
    private PermissionCallback callback;
    private TextView tvPermissionTitle;
    private TextView tvPermissionDescription;
    private Button btnGrantPermission;
    private Button btnSkip;

    public interface PermissionCallback {
        void onAllPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    public static PermissionRequestDialog newInstance() {
        return new PermissionRequestDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (PermissionCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " phải implement PermissionCallback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_permission_request, container, false);

        tvPermissionTitle = view.findViewById(R.id.tvPermissionTitle);
        tvPermissionDescription = view.findViewById(R.id.tvPermissionDescription);
        btnGrantPermission = view.findViewById(R.id.btnGrantPermission);
        btnSkip = view.findViewById(R.id.btnSkip);

        btnGrantPermission.setOnClickListener(v -> requestCurrentPermission());
        btnSkip.setOnClickListener(v -> skipCurrentPermission());

        setCancelable(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkAndRequestNextPermission();
    }

    private void checkAndRequestNextPermission() {
        List<String> requiredPermissions = getRequiredPermissions();

        // Kiểm tra nếu đã hoàn thành tất cả quyền
        if (currentPermissionIndex >= requiredPermissions.size()) {
            finishPermissionProcess();
            return;
        }

        // Lấy quyền hiện tại
        String permission = requiredPermissions.get(currentPermissionIndex);

        // Nếu đã có quyền, chuyển đến quyền tiếp theo
        if (isPermissionGranted(permission)) {
            currentPermissionIndex++;
            checkAndRequestNextPermission();
            return;
        }

        // Hiển thị thông tin về quyền hiện tại
        updatePermissionInfo(permission);
    }

    private void updatePermissionInfo(String permission) {
        String permissionName = getPermissionName(permission);
        tvPermissionTitle.setText("Cấp quyền " + permissionName);
        tvPermissionDescription.setText(PERMISSION_DESCRIPTIONS.getOrDefault(permission,
                "Ứng dụng cần quyền này để hoạt động đúng cách"));
    }

    private String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Đọc bộ nhớ";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Ghi bộ nhớ";
            case Manifest.permission.READ_MEDIA_IMAGES:
                return "Truy cập ảnh";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Thông báo";
            default:
                return permission.substring(permission.lastIndexOf(".") + 1);
        }
    }

    private void requestCurrentPermission() {
        List<String> requiredPermissions = getRequiredPermissions();

        if (currentPermissionIndex < requiredPermissions.size()) {
            String permission = requiredPermissions.get(currentPermissionIndex);

            // Kiểm tra nếu cần giải thích quyền
            if (shouldShowRequestPermissionRationale(permission)) {
                showPermissionRationale(permission);
            } else {
                // Yêu cầu quyền
                requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void showPermissionRationale(String permission) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cần quyền")
                .setMessage(PERMISSION_DESCRIPTIONS.getOrDefault(permission,
                        "Ứng dụng cần quyền này để hoạt động đúng cách"))
                .setPositiveButton("Cấp quyền", (dialog, which) -> {
                    requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Không, cảm ơn", (dialog, which) -> {
                    skipCurrentPermission();
                })
                .create()
                .show();
    }

    private void skipCurrentPermission() {
        currentPermissionIndex++;
        checkAndRequestNextPermission();
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Quyền đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
            }

            // Chuyển đến quyền tiếp theo
            currentPermissionIndex++;
            checkAndRequestNextPermission();
        }
    }

    private void finishPermissionProcess() {
        List<String> requiredPermissions = getRequiredPermissions();

        // Kiểm tra quyền
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.isEmpty()) {
            callback.onAllPermissionsGranted();
        } else {
            callback.onPermissionsDenied(deniedPermissions);
        }

        dismiss();
    }

    public static void checkAndRequestPermissions(FragmentActivity activity) {
        // Kiểm tra nếu là Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean allPermissionsGranted = true;

            // Lấy danh sách quyền cần kiểm tra
            List<String> permissions = getRequiredPermissions();

            // Kiểm tra từng quyền
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                PermissionRequestDialog dialog = PermissionRequestDialog.newInstance();
                dialog.show(activity.getSupportFragmentManager(), "permission_dialog");
            }
        } else {
            // Yêu cầu quyền theo cách truyền thống cho Android < 8.0
            List<String> permissionsToRequest = new ArrayList<>();
            List<String> permissions = getRequiredPermissions();

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) !=
                        PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(activity,
                        permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }
}